----------------------------
AllCrop Goals and Philosophy
----------------------------
This mod intends to provide ways of obtaining every possible crop given only saplings as a starting resource.
While intended for skyblock and survival island environments, it's possible it may find other uses as well.

New crops are obtainable by "cross-cropping" previously attained crops. To start, putting saplings on either side
of a block of dirt, and bone-mealing the saplings, will eventually turn the dirt block into a grass block. This
grass can then be bone-mealed, and wheat seeds will be obtained. By cross-cropping wheat with saplings and wheat
with grass, you can obtain further crop types.

The crops that are obtainable by default are:
* all variety of saplings from oak saplings
* grass block
* wheat
* carrots
* potatoes
* beetroot
* pumpkin
* melon
* mycelium
* brown mushroom
* red mushroom
* cactus
* sugar cane
* kelp
* cocoa beans
* soul sand
* nether wart
* snow layers
* chorus plant

I'm debating adding a toggle in the configuration that allows mob drops to be mutated as well, to both assist
packs where getting passive mobs is difficult, where the nether is difficult, or peaceful mode.

-------------------------------------------
Source installation information for modders
-------------------------------------------
This code follows the Minecraft Forge installation methodology. It will apply
some small patches to the vanilla MCP source code, giving you and it access 
to some of the data and functions you need to build a successful mod.

Note also that the patches are built against "unrenamed" MCP source code (aka
srgnames) - this means that you will not be able to read them directly against
normal code.

Source pack installation information:

Standalone source installation
==============================

See the Forge Documentation online for more detailed instructions:
http://mcforge.readthedocs.io/en/latest/gettingstarted/

Step 1: Open your command-line and browse to the folder where you extracted the zip file.

Step 2: You're left with a choice.
If you prefer to use Eclipse:
1. Run the following command: "gradlew genEclipseRuns" (./gradlew genEclipseRuns if you are on Mac/Linux)
2. Open Eclipse, Import > Existing Gradle Project > Select Folder 
   or run "gradlew eclipse" to generate the project.
(Current Issue)
4. Open Project > Run/Debug Settings > Edit runClient and runServer > Environment
5. Edit MOD_CLASSES to show [modid]%%[Path]; 2 times rather then the generated 4.

If you prefer to use IntelliJ:
1. Open IDEA, and import project.
2. Select your build.gradle file and have it import.
3. Run the following command: "gradlew genIntellijRuns" (./gradlew genIntellijRuns if you are on Mac/Linux)
4. Refresh the Gradle Project in IDEA if required.

If at any point you are missing libraries in your IDE, or you've run into problems you can run "gradlew --refresh-dependencies" to refresh the local cache. "gradlew clean" to reset everything {this does not affect your code} and then start the processs again.

Should it still not work, 
Refer to #ForgeGradle on EsperNet for more information about the gradle environment.
or the Forge Project Discord discord.gg/UvedJ9m

Forge source installation
=========================
MinecraftForge ships with this code and installs it as part of the forge
installation process, no further action is required on your part.

LexManos' Install Video
=======================
https://www.youtube.com/watch?v=8VEdtQLuLO0&feature=youtu.be

For more details update more often refer to the Forge Forums:
http://www.minecraftforge.net/forum/index.php/topic,14048.0.html
