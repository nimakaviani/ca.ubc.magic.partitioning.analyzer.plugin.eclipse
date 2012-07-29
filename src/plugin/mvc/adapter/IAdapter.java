package plugin.mvc.adapter;

import java.util.Map;

public interface 
IAdapter 
{
	public String[] getQueryKeys();
	public Object[] adapt( Map<String, Object> objs );
}
