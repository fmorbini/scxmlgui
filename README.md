This is an attempt to build a graphical user interface for
editing SCXML finite state machines.

We are using the JGraphX library as a base and we started
by modifying the graph editor application included as an
example with JGraphX.

Main features:
  * edit of scxml networks
  * support for src and xi:include
  * search function using Apache Lucene
  * autolayout and possibility to save manual layout
  * export to DOT (graphviz) format
  * scxml listener that highlights and logs events as they happen during the finite state machine execution. (see https://github.com/fmorbini/scxmlgui/blob/master/extra/MySCXMLListener.java as an example of an Apache scxml listener class that sends the proper messages to the editor)

A [short guide](https://github.com/fmorbini/scxmlgui/blob/wiki/Guide.md) is available.

**Main issues/TODOs**: see the [Issues](https://github.com/fmorbini/scxmlgui/issues) tab.
  * Please consider contributing to the code when submitting an issue, as i have very limited time to work on this project and help is appreciated. Thanks!

**To test**: execute the latest jar (requires at least Java 1.6) https://github.com/fmorbini/scxmlgui/blob/master/extra/fsm-editor.jar

**To compile**: cd in the root directory (trunk) and run "ant".

Here some **screenshots**:

Editor window for edge properties:

![edge editing](https://github.com/fmorbini/scxmlgui/blob/master/extra/edge-editing.png)

Context menu to select edit operations on a node:

![node menu](https://github.com/fmorbini/scxmlgui/blob/master/extra/node-menu.png)

The find tool in action:

![find tool](https://github.com/fmorbini/scxmlgui/blob/master/extra/find-tool.png)
