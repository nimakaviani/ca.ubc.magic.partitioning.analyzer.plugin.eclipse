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
	( String... query_keys )
	{
		this.keys
			= query_keys;
	}
	@Override
	public String[] 
	getKeys() 
	{
		return this.keys.clone();
	}

	@Override
	public Object[] 
	adapt
	( Map<String, Object> objs, Object arg) 
	{
		// arguments have to be returned in the right order
		// so we can't simply iterate over the value set
		List<Object> objs_list 
			= new ArrayList<Object>(10);
		for( String key: this.keys){
			objs_list.add( objs.get(key) );
		}
		
		return objs_list.toArray(new Object[objs_list.size()]);
	}

}
