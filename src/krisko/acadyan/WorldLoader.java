package krisko.acadyan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;

public class WorldLoader
{
// Save
	public static boolean save(World world, String mapName, boolean giveWarningWhenMapExists)
	{
		int left = world.getLeftX();
		int right = world.getRightX();
		
		if(left == right)
			return false;
		
		// write blocks to list (for faster saving)
		String[] blockLine = new String[Block.blocks.length];
		for(int i = left; i <= right; i++)
		{
			for(int j = 0; j < Chunk.HEIGHT; j++)
			{
				for(int l = 0; l < 2; l++)
				{
					Block block = world.getBlock(l, i, j);
					if(block == Block.air)
						continue;
					
					if(blockLine[block.id] == null)
						blockLine[block.id] = "block:" +Game.integerToString(block.id, 251);
					
					/*
					 * block:block_id|pos.data.data|-pos,metadata|...
					 * 
					 * Seperator / Signs:
					 * \n -> (char)255
					 * | seperator for blocks -> (char)254
					 * - (before a number) -> (char)253
					 * , (before metadata) -> (char)252
					 * . (before blockdata) -> (char)251
					 */
					
					// write position (with extra sign for - )
					int pos = (i-left) + j*(right - left + 1);
					blockLine[block.id] += (char)254;
					if(pos >= 0)
						blockLine[block.id] += Game.integerToString(pos, 251);
					else
					{
						blockLine[block.id] += (char)253;
						blockLine[block.id] += Game.integerToString(-pos, 251);
					}
					
					// write metadata
					int metadata = world.getMetadata(l, i, j);
					if(metadata > 0)
					{
						blockLine[block.id] += (char)252;
						blockLine[block.id] += Game.integerToString(metadata, 251); 
					}
					
					// write blockdata
					BlockData data = world.getBlockData(l, i, j);
					if(data != null)
					{
						for(int k = 0; k < data.size(); k++)
						{
							blockLine[block.id] += (char)251;
							blockLine[block.id] += data.getData(k);
						}
					}
				}
			}
		}
		
		// create file path
		final String fileName = mapName +".mp";
		final String pathFolder = Acadyan.FolderPath +Acadyan.FileSeperator +"Maps";
		final String pathFile = pathFolder +Acadyan.FileSeperator +fileName;
		
		// create file
		File file = new File(pathFolder);
		if(!file.exists())
		{
			if(!file.mkdirs())
			{
				// something went wrong
				// TODO:
				return false;
			}
		}
		
		file = new File(pathFile);
		if(!file.exists())
		{
			try
			{
				if(!file.createNewFile())
				{
					// something went wrong
					return false;
				}
			} catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		else if(giveWarningWhenMapExists)
		{
			int i = JOptionPane.showConfirmDialog(null, Strings.MAP_EXISTS_REPLACE.get(), Strings.SAVE_MAP_AS.get(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(i != 0)
				return false;
		}
		
		/** First, write text to a string, then convert that string to bytes, encode it and write it to the file */
		
		// write size
		String text = world.getLeftMapIndex() +"|" +world.getRightMapIndex() +(char)255;
		
		// write blocks, metadata and blockdata
		for(int i = 0; i < blockLine.length; i++)
			if(blockLine[i] != null)
				text += blockLine[i] +(char)255;
		
		// convert string to bytes
		byte bytes[] = Game.stringToByte(text);
		
		// now encode the string
		encode(bytes);
		
		// write bytes to the file
		try
		{
			FileOutputStream fOut = new FileOutputStream(file);
			fOut.write(bytes);
			fOut.close();
		} catch(FileNotFoundException ex)
		{
			ex.printStackTrace();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
		
		return true;
	}
	
// Load
	public static boolean loadExtern(World world, String mapName)
	{
		InputStream is;
		try
		{
			is = new FileInputStream(Acadyan.FolderPath +Acadyan.FileSeperator +"Maps" +Acadyan.FileSeperator +mapName +".mp");
		} catch(FileNotFoundException ex)
		{
			return false;
		}
		
		return load(world, is);
	}
	
	public static boolean loadIntern(World world, String mapName)
	{
		return load(world, World.class.getResourceAsStream("/maps/" +mapName +".mp"));
	}
	
	private static boolean load(World world, InputStream fIn)
	{
		int fileLength;
		
		try
		{
			fileLength = fIn.available();
		} catch(IOException ex)
		{
			ex.printStackTrace();
			return false;
		}

		boolean success = true;
		
		world.reset();
		
		/** First, get bytes from file, decode these bytes, convert bytes to string, transfer string to world */
		
		// get bytes
		byte bytes[] = new byte[fileLength];
		try
		{
			int i, index = 0;
			while((i = fIn.read()) != -1)
				bytes[index++] = (byte)i;
			fIn.close();
		} catch(IOException ex)
		{
			world.reset();
			ex.printStackTrace();
			return false;
		}
		
		// decode bytes
		decode(bytes);
		
		// convert to string
		String text = Game.byteToString(bytes);
		
		int sepIndex = 0;
		int line = 0;
		while((sepIndex = text.indexOf((char)255, sepIndex)) != -1)
		{
			line++;
			sepIndex++;
		}
		
		String strings[] = new String[line];
		sepIndex = 0;
		for(int i = 0; i < line; i++)
		{
			strings[i] = text.substring(sepIndex, (sepIndex = text.indexOf((char)255, sepIndex)));
			sepIndex++;
		}
		line = 0;
		
		// read map size
		int leftMapIndex = 0;
		int rightMapIndex = 0;
		if(line < strings.length)
		{
			String s = strings[line++];
			
			try
			{
				leftMapIndex = Integer.parseInt(s.substring(0, s.indexOf('|')));
				rightMapIndex = Integer.parseInt(s.substring(s.indexOf('|') + 1));
				
				world.createNewChunks(leftMapIndex, rightMapIndex);
			} catch(NumberFormatException ex)
			{
				success = false;
			}
		}
		
		int mapWidth = (rightMapIndex - leftMapIndex + 1) * Chunk.WIDTH;
		
		int x, y, blockID, blockPos, lastIndex, index;
		
		// read lines
		for(; line < strings.length; line++)
		{
			index = strings[line].indexOf(':');
			if(index == -1)
				continue;
			
			String syn = strings[line].substring(0, index);
			String s = strings[line].substring(index+1);
			
			/*
			 * block:block_id|pos.data.data|-pos,metadata|...
			 * 
			 * Seperator / Signs:
			 * \n -> (char)255
			 * | seperator for blocks -> (char)254
			 * - (before a number) -> (char)253
			 * , (before metadata) -> (char)252
			 * . (before blockdata) -> (char)251
			 */
			
			if(syn.equals("block"))
			{
				// get block id
				index = s.indexOf((char)254);
				if(index == -1)
					continue;
				
				blockID = Game.stringToInteger(s.substring(0, index), 251);
				s = s.substring(index+1);
				
				// loop for block position/metadata/blockdata
				lastIndex = index = 0;
				
				while(index != -1)
				{
					index = s.indexOf((char)254, lastIndex);
					
					String sVal; // = info about block position/metadata/blockdata (between |...|)
					if(index == -1)
						sVal = s.substring(lastIndex);
					else
						sVal = s.substring(lastIndex, index);
					
					boolean isMinusPos = sVal.indexOf((char)253) >= 0;
					
					String values[] = Game.split(sVal, (char)251); // split (pos,metadata), (data), (data), (data)...
					/* [0] = pos, metadata
					 * [1] = data
					 * [2] = data
					 * ...
					 */
					String posMetadata[] = Game.split(values[0], (char)252);
					/*
					 * [0] = pos
					 * [1] = metadata
					 */
					
					// get position
					if(!isMinusPos)
						blockPos = Game.stringToInteger(posMetadata[0], 251);
					else
						blockPos = -Game.stringToInteger(posMetadata[0].substring(1), 251);
					
					x = (blockPos % mapWidth) + (leftMapIndex*Chunk.WIDTH);
					y = blockPos / mapWidth;
					
					// set block
					if(posMetadata.length == 1) // no metadata available
					{
						world.setBlock(x, y, Block.blocks[blockID]);
					}
					else // metadata is available
					{
						world.setBlock(x, y, Block.blocks[blockID], Game.stringToInteger(posMetadata[1], (char)251));
					}
					
					// set blockdata
					if(values.length > 1)
					{
						BlockData data = world.getBlockData(Block.blocks[blockID].getLayer(), x, y);
						for(int i = 1; i < values.length; i++)
							data.setData(i-1, values[i]);
					}
					
					lastIndex = index + 1;
					
					// only take the first coordinate if it's the start or the goal (3 / 4)
					if(blockID == Block.start.id || blockID == Block.goal.id)
						break;
				}
			}
		}
		
		if(!success)
			world.reset();
		
		return success;
	}
	
// Encode / Decode
	private static void encode(byte bytes[])
	{
		if(bytes.length == 0)
			return;
		
		// convert bytes into int
		int data[] = new int[bytes.length];
		for(int i = 0; i < bytes.length; i++)
			data[i] = bytes[i] & 0xFF;
		
		// start encoding
		int b, i;
		
		// step 1
		for(i = 0; i < data.length; i++)
		{
			b = data[i];
			
			b ^= i % 2 == 0 ? 138 : 94;
			b ^= i % 255;
			
			data[i] = b;
		}
		
		// step 2
		b = data[0];
		for(i = 2; i < data.length; i += 2)
		{
			data[i] ^= data[i-2];
			data[i-2] = data[i];
		}
		b ^= data[i-2];
		data[i-2] = b;
		
		// convert int into bytes
		for(i = 0; i < data.length; i++)
			bytes[i] = (byte)data[i];
	}
	
//	private static void encodeNew(byte bytes[])
//	{
//		if(bytes.length == 0)
//			return;
//		
//		// convert bytes into int
//		int data[] = new int[bytes.length];
//		for(int i = 0; i < bytes.length; i++)
//			data[i] = bytes[i] & 0xFF;
//		
//		// start encoding
//		int b, i;
//		
//		// step 1
//		for(i = 0; i < data.length; i++)
//		{
//			b = data[i];
//			
//			b ^= i % 2 == 0 ? 138 : 94;
//			b ^= i % 255;
//			
//			data[i] = b;
//		}
//		
//		// step 2
//		b = data[0];
//		for(int j = 3; j >= 2; j++)
//		{
//			for(i = j; i < data.length; i += 2)
//			{
//				data[i] ^= data[i-2];
//				data[i-2] = data[i];
//			}
//		}
//		b ^= data[i-2];
//		data[i-2] = b;
//		
//		// convert int into bytes
//		for(i = 0; i < data.length; i++)
//			bytes[i] = (byte)data[i];
//	}
	
	private static void decode(byte bytes[])
	{
		if(bytes.length == 0)
			return;
		
		// convert bytes into int
		int data[] = new int[bytes.length];
		for(int i = 0; i < bytes.length; i++)
			data[i] = bytes[i] & 0xFF;
		
		// start decoding
		int b, i;
		
		// step 2
		i = data.length - ((data.length % 2) ^ 1) - 1;
		b = data[i];
		if(data.length > 2)
		{
			b ^= data[i-2];
			for(; i >= 4; i -= 2)
				data[i] = data[i-2] ^ data[i-4];
			data[i] = data[i-2] ^ b;
		}
		data[0] = b;
		
		// step 1
		for(i = 0; i < data.length; i++)
		{
			b = data[i];

			b ^= i % 255;
			b ^= i % 2 == 0 ? 138 : 94;
			
			data[i] = b;
		}
		
		// convert int into bytes
		for(i = 0; i < data.length; i++)
			bytes[i] = (byte)data[i];
	}
}