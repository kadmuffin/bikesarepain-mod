# BikesArePain Mod

This mod was created to I can do physical exercise while playing Minecraft. It introduces a functional in-game bicycle
that can be optionally linked to a real-world fitness bike, allowing players to translate their physical pedaling into
in-game movement.

## Development Status

The mod is currently in very alpha stage. Core functionalities are implemented, but further refinement, optimizations
and feature additions are planned. As this is my first Minecraft mod, I most likely implemented things in a
less-than-optimal way. I'm open to suggestions and contributions.

## Technical Details

This mod is implemented using Architectury for multi-loader compatibility and uses the Mojmaps mappings. For reading the
serial data from the arduino, I am using jSerialComm.

### Some of the Known Issues

- Pitch is synced from clients to the server (should be the other way around)
- HUD display item and "Modifier: Floating on Water" is only available from the creative menu
- Code for commands is kinda of a mess
- No version validation in the client or server
- The mod doesn't auto disable when entering a world without the mod installed
- Serial is activated when entering a world, but in a buggy way, requiring running `/bikes close` and `/bikes open` to
  fix it
- Data related to fitness bike linking is not saved to disk
- The model's part names is a mess (as I started this as something for my own personal use, I didn't care much about it)
- Running the neoforge client gradle task fails, but the neoforge build task works fine
- The bike's model parts are animated using a `RenderLayer`, which I imagine isn't ideal

### Planned Features

- Code refactoring for better performance and maintainability
- Renaming the model's bone for better readability
- UI for visualizing workout session data
- Improving the user experience for the mod
- Saving workout session data to disk
- Configurable workout session goals
- Advancements for completing workout session goals
- Crafting recipes for items that don't have one yet
- Adding more modifiers for the bike
- Adding more bikes (mountain bike, road bike, motorbike, etc.)
- Server-side configuration for features

## Installation

For just using the bikes included in the mod, you can download the latest JAR file from Modrinth and place it in your
mods folder (remember to download the dependencies in [Dependencies](#Dependencies)). If you want to also use the
fitness bike feature, you will need to know your way around using Arduino and electronics. Right now there isn't a guide
on how to set it up, but you can read the arduino sketch file that I used for my setup in the repo's `arduino` folder.

## Contributing

Contributions are welcome. Please open an issue for major changes to discuss what you would like to change.

## Dependencies

This mod requires `Geckolib`, `YetAnotherConfigLib` and the `ArchitecturyAPI` mod to work. The `Geckolib` mod is used
for animations, the `YetAnotherConfigLib` mod is used for configuration, and the `ArchitecturyAPI` mod is used for
multi-loader compatibility.

## License

This mod is currently licensed under the MIT License. See the [LICENSE](LICENSE) file for details. I still haven't
decided the licensing for the models included in the mod. Please see the [THIRD_PARTY_LICENSES](THIRD_PARTY_LICENSES.md)
file for details on third-party software used in this mod.