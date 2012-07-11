There are some things that are interesting about my implementation:
	1) consider what is mentioned in
	http://www.techrepublic.com/article/mvc-design-pattern-brings-about-better-organization-and-code-reuse/1049862
	
	Here we read that the view does not make decisions about how events are handled. Instead it passes
	decisions on to the controller, which then makes the appropriate calls to the model. The controller 
	is also responsible for things like formatting.
	
	This is all done in order to reduce the amount of computation performed by the view, and to make the
	connection between the view and the model less strong. There are, however, many ways to ensure this
	separation, and they differ in how closely the view and the controller are tied. If we have a very 
	strong connection, where the controller makes all the decisions for the view, we risk making
	a controller that is very specific to a particular view. 
	
	If instead we do as I have opted, and attempt to decouple the view from the controller AS WELL, so
	that we can not only connect views and models in various combinations statically at compile time,
	but also switch between views and controllers dynamically at run time, we cannot tie the view and
	controller so strongly together, and it becomes more and more difficult to shift all of the
	decision making responsibility to the to controller. That is one of the ways in which my design is
	interesting. I made this choice because the controller needed to serve as a communication point
	between views, and which views communicate with one another may change as the application runs.
	2) This way of thinking about MVC, in my opinion, fits more naturally, or more obviously, with
	the way adapters and other objects in SWT allow decision logic to be placed directly in the view.
	
	3) Actually, the implementation is quite nice. It splits the view and controller without expecting
	the view to do much more than receive and generate events at certain times. The quirky thing is 
	that this implementation is all-in on the idea of the controller as a communication point: it is
	not a decision maker, only a tunnel. The model is the decision maker, with the view simply 
	deciding when to send and receive.
	
06062012:

This is a new readme containing some thoughts and objectives. The plugin project current represents the
state of the art when it comes to the snapshot viewer plugin. Today I will begin work on the partitioner
in the hopes that getting the models to display correctly will provide a "wow" reaction (sort of, not
really). Then I will focus on connecting the two parts and gluing them together. I have also 
made note of the technical debt present in the snapshots portion of the project (in the form of TODO
statements).


Bundle-NativeCode: lpsolve55.dll; lpsolve55j.dll; osname=win32; processor=x86_64;

