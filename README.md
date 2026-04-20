# Redmon

Redmon is a tool that allows you to create beautiful and functional debug overlays for your redstone projects. It is 
intended for practitioners of computational redstone, but can be used for just about any redstone project!

Redmon currently supports the following signals:

- Repeaters (on/off)
- Comparators (on/off)
- Torches (on/off)
- Redstone dust (on/off)
- Redstone dust (signal strength)

## Quickstart guide

### Hotkeys

The mod has a small number of dedicated hotkeys for common operations.

- `=` - Toggle visibility of the mod overlay.
- `[` - Show previous profile page.
- `]` - Show next profile page.

These hotkeys may be configured in the game settings.

### Creating a profile

Redmon functions using "profiles". Before any signals can be created or viewed, a profile must be created to contain
them. Each profile is intended to correspond to a specific project or component.

`/redmon profile create <profile-name>`

This will create an empty profile with your specified name and set it as the currently selected profile with respect to
your current position. After running this command, you should see the profile in the top left hand corner of your 
screen like so:

![image](images/created-profile.png)

- `new_page` - The current page the profile is on (default name).
- `test` - The name of the created profile.

Profiles are stored on disk at the following path `.minecraft/config/redmon_profiles.json` and are saved automatically
as you make changes. You can view your saved profiles with the `/redmon profile list` and `/redmon profile search`
commands.

Existing profiles may be selected as follows:

`/redmon profile select <profile-name>`

Whenever a profile is selected, it is "mapped" onto the world relative to the current player position. This allows
profiles to be applied to designs regardless of their position in the world. It is recommended to mark the position
where the profile was created so you know where to stand when re-selecting it later!

If components are missing or the profile has been selected at the wrong location, and the mod is consequently unable to 
find one or more blocks, errors will appear in the overlay like so:

![image](images/multibit-signal-error.png)

You can also add additional pages to your profile which you can cycle through using [hotkeys](#hotkeys):

`/redmon page add <page-name>`

Finally, when you're done with the selected profile, you can run `/redmon profile deselect` to return the UI to its
default state.

### Adding signals

A signal is made up of a block (or set of blocks) that you wish to monitor in the overlay. Signals are added by looking 
at a block or wire and running the `/redmon signal add` command to add the signal to the currently active profile:

`/redmon signal add <signal-name> <signal-type> <block-count>`

There are currently 5 signal types to choose from:

- `repeater` - Redstone repeater.
- `torch` - Redstone torch.
- `dust` - Redstone dust.
- `dust_ss` - Redstone dust (signal strength).
- `comparator_binary` - Comparator.

The following screenshot depicts the outcome of running the command `/redmon signal create dust dust_binary 1` while
looking a section of redstone dust. This command creates a single redstone dust signal with the name "dust":

![image](images/created-signal.png)

It is also possible to add signals with multiple blocks in a single command. This is done by aiming the reticule at the
component that represents the most significant bit in the desired signal, and passing a block count greater than one,
like so:

`/redmon signal create repeaters repeater 8`

![image](images/created-multibit-signal.png)

When creating a multi-block signal like this, the mod will search for additional matching blocks along the cardinal 
direction which is closest to the player look angle. If the requested number of blocks aren't found, an error will be
displayed in the console. Be sure to face in the right direction when using this command!

Signals can also be removed, moved, renamed or reformatted using the following commands:

- `/redmon signal remove <signal-name>`
- `/redmon signal move <signal-name> (up|down) [<count>]`
- `/redmon signal move <signal-name> column <column-number>`
- `/redmon signal rename <signal-name> <new-signal-name>`
- `/redmon singal format <signal-name> <signal-format>`

## Limitations

- The mod is currently purely client-side, relying on block states to determine the state of components. This _works_
  but means that the mod can't see certain information (like the signal strength emitted by a comparator). For this to
  work, a server side component would be required.
- The mod currently only supports the fabric runtime (forge is not currently supported).

## Roadmap

There are a number of considered/planned features which are not currently included within the mod:

- Profile sharing.
- Support for large memory banks.
- Parameterizable assembler/disassembler for printing instruction disassembly in the overlay.
- 3D overlay to show positions of signals directly in the world.
- Server side component for more efficient and accurate signal updates.

## Dedication

To the RDF, the community where I got my first taste of computer science.

ORE is also very very cool ;)
