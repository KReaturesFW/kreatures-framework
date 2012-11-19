package angerona.fw;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import angerona.fw.internal.Entity;
import angerona.fw.internal.IdGenerator;

/**
 * Base class for special extensions of the agent model. Every subclass
 * of BaseAgentComponent implements a special data component like 
 * Secrecy Knowledge or Know-How.
 * 
 * This base class provides the unique id concept for entities which is
 * used by the Angerona report system for example.
 * 
 * It also provides an interface to inform listeners of data components 
 * about changes of the data. So it implements an abstract model object
 * in a MVC Pattern.
 * @author Tim Janus
 *
 */
public abstract class BaseAgentComponent implements AgentComponent {
	
	/** unique id of the parent (the agent) */
	private Long parentId;
	
	/** own unique id */
	private Long id;
	
	/** how deep is this instance in the copy hierachy. 0 means its original, 1 its 
	 *  a copy of the orignal and 2 that it is a copy of the copy of the original.
	 */
	private int copyDepth;
	
	/** implementation for property change */
	private PropertyChangeSupport propertyChangeSupport;
	
	/** Default Ctor: Initializes the ids and copy-depth */
	public BaseAgentComponent() {
		id = IdGenerator.generate(this);
		copyDepth = 0;
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	/** Copy Ctor: Creates the component by copying the component from the given other component */
	public BaseAgentComponent(BaseAgentComponent other) {
		this.id = new Long(other.id);
		this.parentId = new Long(other.parentId);
		copyDepth = other.copyDepth + 1;
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	/**
	 * Adds the given listener to the registered listeners
	 * @param listener	Reference to the listener to add
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	/**
	 * Removes the given listener from the list of registered listeners
	 * @param listener	Reference to the listener to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	
	/**
	 * Fires a property change event. This method is used by sub classes to inform
	 * listeners about changes of the data.
	 * @param propertyName		The name of the property which is changed
	 * @param oldValue			The old value of the property
	 * @param newValue			The new value of the property
	 */
	protected void firePropertyChangeListener(String propertyName, 
			Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	@Override
	public void setParent(Long id) {
		parentId = id;
	}

	@Override
	public Agent getAgent() {
		Entity reval = IdGenerator.getEntityWithId(parentId);
		if(reval != null) {
			return (Agent)reval;
		}
		return null;
	}
	
	public void report(String msg) {
		/** unit tests will run without an agent on the component so test for the agent before reporting to angerona */
		if(getAgent() != null)
			Angerona.getInstance().report(msg, getAgent(), this);
	}
		
	@Override
	public void init(Map<String, String> additionalData) { }
	
	@Override
	public Long getGUID() {
		return id;
	}

	@Override
	public Long getParent() {
		return parentId;
	}

	@Override
	public List<Long> getChilds() {
		return new LinkedList<Long>();
	}
	
	@Override
	public int getCopyDepth() {
		return copyDepth;
	}
	
	@Override
	public abstract Object clone();
}
