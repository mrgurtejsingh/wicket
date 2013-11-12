/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.settings.def;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IComponentAwareEventSink;
import org.apache.wicket.IDetachListener;
import org.apache.wicket.IEventDispatcher;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Framework settings for retrieving and configuring framework settings.
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class FrameworkSettings implements IEventDispatcher
{
	private IDetachListener detachListener;

	private List<IEventDispatcher> eventDispatchers = null;

	/**
	 * The {@link ISerializer} that will be used to convert the pages to/from byte arrays
	 */
	private ISerializer serializer;

	/**
	 * Construct.
	 * 
	 * @param application
	 */
	public FrameworkSettings(final Application application)
	{
		serializer = new JavaSerializer(application.getApplicationKey());
	}

	/**
	 * Gets the Wicket version. The Wicket version is in the same format as the version element in
	 * the pom.xml file (project descriptor). The version is generated by maven in the build/release
	 * cycle and put in the wicket.properties file located in the root folder of the Wicket jar.
	 *
	 * The version usually follows one of the following formats:
	 * <ul>
	 * <li>major.minor[.bug] for stable versions. 1.1, 1.2, 1.2.1 are examples</li>
	 * <li>major.minor-state for development versions. 1.2-beta2, 1.3-SNAPSHOT are examples</li>
	 * </ul>
	 *
	 * @return the Wicket version
	 */
	public String getVersion()
	{
		String implVersion = null;
		Package pkg = getClass().getPackage();
		if (pkg != null)
		{
			implVersion = pkg.getImplementationVersion();
		}
		return Strings.isEmpty(implVersion) ? "n/a" : implVersion;
	}

	/**
	 * @return detach listener or <code>null</code> if none
	 */
	public IDetachListener getDetachListener()
	{
		return detachListener;
	}

	/**
	 * Sets detach listener
	 *
	 * @param detachListener
	 *            listener or <code>null</code> to remove
	 */
	public void setDetachListener(IDetachListener detachListener)
	{
		this.detachListener = detachListener;
	}

	/**
	 * Registers a new event dispatcher
	 *
	 * @param dispatcher
	 */
	public void add(IEventDispatcher dispatcher)
	{
		Args.notNull(dispatcher, "dispatcher");
		if (eventDispatchers == null)
		{
			eventDispatchers = new ArrayList<IEventDispatcher>();
		}
		if (!eventDispatchers.contains(dispatcher))
		{
			eventDispatchers.add(dispatcher);
		}
	}

	/**
	 * Dispatches event to registered dispatchers
	 * 
	 * @see IEventDispatcher#dispatchEvent(Object, IEvent, Component)
	 * 
	 * @param sink
	 * @param event
	 * @param component
	 */
	@Override
	public void dispatchEvent(Object sink, IEvent<?> event, Component component)
	{
		// direct delivery
		if (component != null && sink instanceof IComponentAwareEventSink)
		{
			((IComponentAwareEventSink)sink).onEvent(component, event);
		}
		else if (sink instanceof IEventSink)
		{
			((IEventSink)sink).onEvent(event);
		}

		// additional dispatchers delivery
		if (eventDispatchers == null)
		{
			return;
		}
		for (IEventDispatcher dispatcher : eventDispatchers)
		{
			dispatcher.dispatchEvent(sink, event, component);
		}
	}

	/**
	 * Sets the {@link ISerializer} that will be used to convert objects to/from byte arrays
	 *
	 * @param serializer
	 *            the {@link ISerializer} to use
	 */
	public void setSerializer(ISerializer serializer)
	{
		this.serializer = Args.notNull(serializer, "serializer");
	}

	/**
	 * @return the {@link ISerializer} that will be used to convert objects to/from byte arrays
	 */
	public ISerializer getSerializer()
	{
		return serializer;
	}
}
