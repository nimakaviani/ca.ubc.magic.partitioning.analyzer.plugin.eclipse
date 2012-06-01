/*
Copyright (c) 2008 Jeff Chapman

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the following
      disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
// $Id: LogAction.java,v 1.1 2008/09/23 04:48:15 jchapman0 Exp $
package events.logging;

import java.awt.event.ActionEvent;

import model_controllers.Constants;




import jipplugin.Activator;

/**
 *
 * @author not attributable
 * @version 1.0
 */
public class 
LogAction 
implements IEventLogAction
{
	@Override
	public void 
	performAction
	(ActionEvent event) 
	{
		System.out.println("log action called");
		
		EventLogEvent event_log_event 
			= (EventLogEvent) event;
		Activator.getDefault().getEventLogListModel().updateLog(
			(String) event_log_event.getProperty( Constants.KEY_LOGS_DATA)
		);
		
	}
}
