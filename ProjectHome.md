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
  * scxml listener that highlights and logs events as they happen during the finite state machine execution. (see http://code.google.com/p/scxmlgui/source/browse/trunk/extra/MySCXMLListener.java as an example of an Apache scxml listener class that sends the proper messages to the editor)

A [short guide](http://code.google.com/p/scxmlgui/wiki/Guide) is available.

**Main issues/TODOs**: see the [Issues](http://code.google.com/p/scxmlgui/issues/list) tab.
  * Please consider contributing to the code when submitting an issue, as i have very limited time to work on this project and help is appreciated. Thanks!

**To test**: execute the latest jar (requires at least Java 1.6) http://scxmlgui.googlecode.com/svn/trunk/extra/fsm-editor.jar

**To download the source**: follow the instructions in the [Source](http://code.google.com/p/scxmlgui/source/checkout) tab.

**To compile**: cd in the root directory (trunk) and run "ant".

**For donations** use [the donate page](http://code.google.com/p/scxmlgui/wiki/donate). Thanks.

Here some **screenshots**:

Editor window for edge properties:

![http://scxmlgui.googlecode.com/svn/trunk/extra/edge-editing.png](http://scxmlgui.googlecode.com/svn/trunk/extra/edge-editing.png)

Context menu to select edit operations on a node:

![http://scxmlgui.googlecode.com/svn/trunk/extra/node-menu.png](http://scxmlgui.googlecode.com/svn/trunk/extra/node-menu.png)

The find tool in action:

![http://scxmlgui.googlecode.com/svn/trunk/extra/find-tool.png](http://scxmlgui.googlecode.com/svn/trunk/extra/find-tool.png)