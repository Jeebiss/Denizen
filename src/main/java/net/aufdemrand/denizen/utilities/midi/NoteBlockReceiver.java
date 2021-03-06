package net.aufdemrand.denizen.utilities.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;

import org.bukkit.Sound;

import com.google.common.collect.Maps;

/**
 * Midi Receiver for processing note events.
 *
 * @author authorblues
 */
public class NoteBlockReceiver implements Receiver
{
    private static final float VOLUME_RANGE = 10.0f;

    private List<dEntity> entities;
    private dLocation location;
    private final Map<Integer, Integer> channelPatches;
    private Collection<dEntity> unusedEntities = new LinkedList<dEntity>();

    public NoteBlockReceiver(List<dEntity> entities) throws InvalidMidiDataException, IOException
    {
        this.entities = entities;
        this.location = null;
        this.channelPatches = Maps.newHashMap();
    }

    public NoteBlockReceiver(dLocation location) throws InvalidMidiDataException, IOException
    {
        this.entities = null;
        this.location = location;
        this.channelPatches = Maps.newHashMap();
    }

    @Override
    public void send(MidiMessage m, long time)
    {
        if (m instanceof ShortMessage)
        {
            ShortMessage smessage = (ShortMessage) m;
            int chan = smessage.getChannel();

            switch (smessage.getCommand())
            {
                case ShortMessage.PROGRAM_CHANGE:
                    int patch = smessage.getData1();
                    channelPatches.put(chan, patch);
                    break;

                case ShortMessage.NOTE_ON:
                    this.playNote(smessage);
                    break;

                case ShortMessage.NOTE_OFF:
                    break;
            }
        }
    }

    public void playNote(ShortMessage message)
    {
        // if this isn't a NOTE_ON message, we can't play it
        if (ShortMessage.NOTE_ON != message.getCommand()) return;

        int channel = message.getChannel();

        // If this is a percussion channel, return
        if (channel == 9) return;

        // get the correct instrument
        Integer patch = channelPatches.get(channel);

        // get pitch and volume from the midi message
        float pitch = (float) ToneUtil.midiToPitch(message);
        float volume = VOLUME_RANGE * (message.getData2() / 127.0f);

        Sound instrument = Sound.NOTE_PIANO;
        if (patch != null) instrument = MidiUtil.patchToInstrument(patch);

        if (location != null) {

            location.getWorld().playSound(location, instrument, volume, pitch);
        }
        else if (entities != null && !entities.isEmpty()) {
            for (dEntity entity : entities) {
                if (entity.isSpawned()) {
                    if (entity.isPlayer()) {
                        entity.getPlayer().playSound(entity.getLocation(), instrument, volume, pitch);
                    }
                    else {
                        entity.getWorld().playSound(entity.getLocation(), instrument, volume, pitch);
                    }
                }
                else {
                    unusedEntities.add(entity);
                }
            }

            // Remove any entities that are no longer spawned
            if (!unusedEntities.isEmpty()) {
                for (dEntity unusedEntity : unusedEntities) {
                    entities.remove(unusedEntity);
                }
                unusedEntities.clear();
            }
        }
        else this.close();
    }

    @Override
    public void close()
    {
        if (entities != null) entities = null;
        if (location != null) location = null;
        channelPatches.clear();
    }
}
