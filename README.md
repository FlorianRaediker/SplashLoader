SplashLoader - Minecraft Forge Mod
==================================

Getting Started
---------------
After installing the Mod, splashes are loaded from every domain. Mods can then add custom splashes to 
`assets/<modid>/texts/splashes.txt`. 

Additionally, SplashLoader supports a custom JSON format which allows you to specify further properties of the splash 
texts such as
 * Translate certain splash texts
 * Show splash texts only on a certain day, next to the "normal" splash texts
 * Show only certain splash texts on a specific day (like Minecraft's "Merry X-mas!", "Happy new year!" and 
   "OOoooOOOoo! Spooky!")
 * Delete splash texts from other domains


Extended JSON-based Splash Format
---------------------------------
An example file which would be located at `assets/<modid>/texts/splashes.json` is shown below (for the full file, see 
[example_splashes.json](example_splashes.json)), along with explanations 
of the format (note that the JSON5 comments are *not* supported):

### Remove splashes from other domains
```json5
{
    "remove": {
        "minecraft": [
            "OOoooOOOoo! Spooky!"  // don't display this splash
        ],
        "some_domain": "*"  // don't display splashes from domain 'some_domain'
    },
```
Splashes may be removed from other domains which are loaded **before** the current domain. So, if you want to remove 
splashes of another Mod, make sure the Mod is being loaded before your Mod.

To remove splashes, specify an object that contains domain names as keys. You can then either remove certain splashes
with an array value or don't display any splashes from that specific domain by using `"*"`. 

For the `minecraft` domain, additional values can be used. If you don't want hard-coded splashes to be shown 
("Merry X-mas!", "Happy new year!" and "OOoooOOOoo! Spooky!"):
```json5
        "minecraft": "hard-coded"
```
`"minecraft": "*"` only removes non-hard-coded splashes. To remove hard-coded splashes as well as splashes from 
`assets/minecraft/texts/splashes.txt`, use the following:
```json5
        "minecraft": "**"
```

### Normal splashes
```json5
    "splashes": [  // contains a list of splashes
        "Some Splash",
        "EXAMPLE!",
```
Specify all splashes in an array with key `"splashes"`.

### Show splashes only on a specific date
```json5
        {
            "date": {"month": "nov", "date": "18"},
            "splashes": ["Happy birthday, Minecraft!"]
        },
```
If splashes need additional information, use an object instead of a string. All properties are inherited by  
all splashes inside the `"splashes"` array. `"splashes"` arrays may be nested: This array can itself contain objects 
and strings. 

The splash in the above example is only shown on November 18, but alongside all other splashes which are shown on every 
date. If the splash should be the only splash shown (like Minecraft's hard-coded splashes), it should look like the 
following:
```json5
        {
            "is_exclusive": true,  // "Happy birthday, Minecraft!" is the only splash shown on November 18 (together 
                                   // with other splashes that are marked as exclusive and are shown on November 18)
            "date": {"month": "nov", "date": "18"},
            "splashes": ["Happy birthday, Minecraft!"]
        },
```

### Translate splashes
```json5
        {
            "translated": true,
            "splashes": [
                "This splash will be translated!" // translated using key 'splash.<modid>.This splash will be translated!'
            ]
        }
    ]
}
```
Add `"translated": true` if you want splashes to be translated. If the translation key does not exist, the splash (and 
not the translation key) is displayed instead.

### Shorthand for only one splash
Instead of `"splashes": ["Happy birthday, Minecraft!"]`, one can also write `"splash": "Happy birthday, Minecraft!"`.

Licensing
--------
This project is licensed under the terms of the [MIT license](LICENSE).
