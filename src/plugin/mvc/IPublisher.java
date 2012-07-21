package plugin.mvc;

public interface 
IPublisher 
{
	public void
	publish
	( Class<?> sender_class, String property_name, Object packet);
	
	public void
	registerPublicationListener
	( Class<?> listener_class, final String property_name, final PublicationHandler publication_handler	);
}
