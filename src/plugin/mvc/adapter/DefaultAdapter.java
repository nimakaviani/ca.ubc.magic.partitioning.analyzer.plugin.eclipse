package plugin.mvc.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class 
DefaultAdapter 
implements IAdapter
{
	private String[] keys;

	public
	DefaultAdapter
	( String[] query_keys )
	{
		this.keys
			= query_keys;
	}
	@Override
	public String[] 
	getQueryKeys() 
	{
		return this.keys;
	}

	@Override
	public Object[] 
	adapt
	( Map<String, Object> objs ) 
	{
		List<Object> objs_list = new ArrayList<Object>(10);
		for( String key: this.keys){
			objs_list.add( objs.get(key));
		}
		return objs_list.toArray(new Object[0]);
	}

}
