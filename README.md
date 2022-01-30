# Countvotes
This is a project written entirely in Java that allows you to count votes.

It is based on a TUI that allows you to dynamically add and remove people, check recent votes (and delete those) and it stores all information in files.

This TUI is written entirely in Java and uses the JCurses library.
Under the hood, it currently utilizes guava and shadow in a gradle environment.

### How to build the program
- run `gradlew shadowJar`
- add `jcurses.jar` to your classpath and put `libcurses64.dll` (or `libcurses.dll` for non-64 bit systems) into the same folder. Both files are in the `/libs` folder.
- run the built jar (`build/libs/countvotes.jar`) with the command `java -cp * com.github.simulatan.countvotes.Main`

### Bugs
...will be tracked in the [GitHub issues](https://github.com/SIMULATAN/countvotes/issues).