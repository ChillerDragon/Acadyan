package krisko.acadyan;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.JOptionPane;

public class SoundManager
{
	private SoundManager(String path, int soundType)
	{
		type = soundType;
		path = "/audio/" +path;
		url = SoundManager.class.getResource(path);
		if(url == null)
			System.out.println("File " +path +" doesn't exist");
		
		if(type == TYPE_BGM)
			clip = createClip();
	}
	
	private Clip createClip()
	{
		if(url == null)
			return null;
		
		try
		{
			AudioInputStream ai = AudioSystem.getAudioInputStream(url);
			final Clip clip = AudioSystem.getClip();
			clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent e) {
					if(e.getType() == LineEvent.Type.STOP)
						clip.close();
				}
			});
			clip.open(ai);
			
			return clip;
		} catch(Exception ex)
		{
			JOptionPane.showMessageDialog(null, ex.toString(), "Couldnt create clip", JOptionPane.QUESTION_MESSAGE);
			return null;
		}
	}
	
	public void play() { play(1.f); }
	
	public void play(final float volume)
	{
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run()
			{
				Clip c = clip == null ? createClip() : clip;
				if(c == null)
					return;
				
				float vol = volume * (type == TYPE_BGM ? Options.volumeBgm : Options.volumeEffects);
				
//				if(vol < 1.f)
					setVolume(c, vol);
				
				c.start();
			}
		});
		thread.start(); // is this save?
	}
	
	public void loop(int count) { loop(count, 1.f); }
	
	public void loop(final int count, final float volume)
	{
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run()
			{
				Clip c = clip == null ? createClip() : clip;
				if(c == null)
					return;
				
				float vol = volume * (type == TYPE_BGM ? Options.volumeBgm : Options.volumeEffects);
				
//				if(vol < 1.f)
					setVolume(c, vol);
				
				c.loop(count);
			}
		});
		thread.start(); // is this save?
	}
	
	public void setVolume(float volume)
	{
		if(clip != null)
			setVolume(clip, volume);
	}
	
	public void stop()
	{
		if(clip != null)
		{
			clip.stop();
			clip.flush();
		}
	}
	
	private void setVolume(Clip clip, float volume)
	{
		if(volume < 0.f)
			volume = 0.f;
		else if(volume > 1.f)
			volume = 1.f;
		
		FloatControl fc = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		float dB = (float)(Math.log(volume) / Math.log(10.0) * 20.0);
		fc.setValue(dB);
	}
	
	public static void load() { }
	
	private URL url;
	private Clip clip;
	private int type;
	
	public static final int TYPE_BGM = 0;
	public static final int TYPE_EFFECTS = 1;

	public static final SoundManager bgm1 = new SoundManager("bgm.wav", TYPE_BGM);
	public static final SoundManager jump = new SoundManager("jump.wav", TYPE_EFFECTS);
	public static final SoundManager click = new SoundManager("click.wav", TYPE_EFFECTS);
	public static final SoundManager punch = new SoundManager("punch.wav", TYPE_EFFECTS);
	public static final SoundManager hit = new SoundManager("hit.wav", TYPE_EFFECTS);
	public static final SoundManager die = new SoundManager("die.wav", TYPE_EFFECTS);
	public static final SoundManager coin1 = new SoundManager("coin1.wav", TYPE_EFFECTS);
	public static final SoundManager coin2 = new SoundManager("coin2.wav", TYPE_EFFECTS);
	public static final SoundManager coin3 = new SoundManager("coin3.wav", TYPE_EFFECTS);
	public static final SoundManager breaking = new SoundManager("break.wav", TYPE_EFFECTS);
	public static final SoundManager pressurePlateOff = new SoundManager("pressure_plate_off.wav", TYPE_EFFECTS);
	public static final SoundManager pressurePlateOn = new SoundManager("pressure_plate_on.wav", TYPE_EFFECTS);
}