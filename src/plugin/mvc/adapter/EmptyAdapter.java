package plugin.mvc.adapter;

import java.util.Map;

public class 
EmptyAdapter 
extends DefaultAdapter
{
	public 
	EmptyAdapter
	( String... query_keys ) 
	{
		super.keys 
			= query_keys;
	}

	@Override
	public Object[] 
	adapt
	( Map<String, Object> objs, Object arg) 
	{
		return new Object[]{};
	}
}
