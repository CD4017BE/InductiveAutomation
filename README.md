InductiveAutomation is a Minecraft mod that adds various technical content to Minecraft. 
It's mainly focused on providing automation systems for large otherwise extremely time consuming or annoying tasks.
Another goal is to rather provide a few multifunctional machines instead of many specialized machines for only very specific tasks.

For more information about the content see [InductiveAutomation Manual](https://dl.dropboxusercontent.com/u/68211316/Sonstiges/InductiveAutomationManual.pdf)

Links:
- [InductiveAutomation on Curse](http://minecraft.curseforge.com/projects/inductive-automation)

#Download & Installation

Choose a [release version](https://github.com/CD4017BE/InductiveAutomation/releases)
- For normal playing with Forge-Modloader download the file that just ends with `.jar`
- For use in deobfuscated ForgeGradle environment download the `-deobf.jar` file
**Important:** Also download the required version of CD4017BE_lib that is linked in the release description.
These files then just go into the `mods` folder of your Minecraft-installation, Modpack or Gradle-project.

## Project setup from source

If you want to program with the mod itself you need an empty ForgeGradle project first. Then either use Git or download the zipped source-code to import it into your project.
As this project depends on my CD4017BE_lib you also need to download the `-deobf.jar` for it and add it to your build path and gradle dependency. Or alternatively setup another ForgeGradle project for it and add that as dependency.
In order to compile the NEI-plugin of this mod you also need to add the deobfuscated versions of NEI, CodeChickenCore and CodeChickenLib to your dependency.

#Modpack permissions

You are allowed to use this mod in any public Modpack you like as long as you provide source credits.