package plugin.mvc;

public interface 
IPublisher 
{
	public void
	publish
	( Class<?> sender_class, Publications publication, Object packet);
	
	public void
	registerPublicationListener
	( Class<?> listener_class, final Publications activeEditorChanged, final PublicationHandler publication_handler	);
}
