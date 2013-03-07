package com.askcs.askservices.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.askcs.askservices.DB;
import com.almende.cape.agent.CapeAgent;
import com.almende.cape.entity.DataSource;
import com.almende.eve.agent.annotation.Name;
import com.almende.eve.agent.annotation.Required;
import com.almende.eve.config.Config;

public class MerlinAgent extends CapeAgent {
	// TODO: move the storage of the data to a flat table database,
	//       having two indexes (userId and dataSource) to improve performance?
	private static final Logger log = Logger
			.getLogger(MerlinAgent.class.toString());	
	
	@Override
	public void create () {
		try {
			Config config = getAgentFactory().getConfig();
			String username = config.get("cape", "merlin", "username");
			String password = config.get("cape", "merlin", "password");
			String resource = config.get("cape", "merlin", "resource");
			
			if (username == null) {
				throw new IllegalArgumentException(
						"Configuration parameter cape.merlin.username missing.");
			}
			if (password == null) {
				throw new IllegalArgumentException(
						"Configuration parameter cape.merlin.password missing.");
			}
			
			setAccount(username, password, resource);
			connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void delete () {
		try {
			disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Find an information source for a dataType 
	 * @param userId               User id.
	 * @param dataType             Type of information, for example "state",
	 *                             "contacts".
	 * @return dataSources         All known information sources providing
	 *                             this data type.
	 * @throws Exception 
	 */
	public List<DataSource> find(
			@Name("userId") String userId, 
			@Required(false) @Name("dataType") String dataType) throws Exception {
		List<DataSource> dataSources = new ArrayList<DataSource>();
		
		GraphDatabaseService db = DB.get();
		Index<Node> index = db.index().forNodes(INDEX_DATA_SOURCE);
		Transaction tx = db.beginTx();
		try {
			for (Node node : index.get(PROPERTY_USER_ID, userId)) {	
				if (dataType == null || 
						dataType.equals(node.getProperty(PROPERTY_DATA_TYPE, null))) {
					DataSource dataSource = nodeToDataSource(node);
					dataSources.add(dataSource);
				}
			}
			
	        tx.success();
		}
		finally {
		    tx.finish();
		}
		
		return dataSources;
	}
	
	/**
	 * Register a datasource
	 * @param dataSource   A data source description, contains a userId and
	 *                     an agentUrl, and a data type
	 */
	public void register(@Name("dataSource") DataSource dataSource) {
		GraphDatabaseService db = DB.get();
		Index<Node> index = db.index().forNodes(INDEX_DATA_SOURCE);
		Transaction tx = db.beginTx();
		try {
			Node node = dataSourceToNode(db, dataSource);
			index.add(node, PROPERTY_USER_ID, dataSource.getUserId());
			
	        tx.success();
		}
		finally {
		    tx.finish();
		}
		log.info("Registered: "+dataSource.getDataType()+" of agent: "+dataSource.getAgentUrl());
	}
	
	/**
	 * Unregister a data source
	 * @param dataSource   A data source description, contains a userId and
	 *                     an agentUrl, and a data type
	 */
	public void unregister(@Name("dataSource") DataSource dataSource) {
		GraphDatabaseService db = DB.get();
		Index<Node> index = db.index().forNodes(INDEX_DATA_SOURCE);
		Transaction tx = db.beginTx();
		try {
			String userId = dataSource.getUserId();
			for (Node node : index.get(PROPERTY_USER_ID, userId)) {
				DataSource nodeDataSource = nodeToDataSource(node);
				if (dataSource.equals(nodeDataSource)) {
					index.remove(node);
					node.delete();
				}				
			}
			
	        tx.success();
		}
		finally {
		    tx.finish();
		}
	}

	/**
	 * Retrieve a list with all registered data sources
	 * @param dataSources     
	 */
	public List<DataSource> list() {
		List<DataSource> dataSources = new ArrayList<DataSource>();
		GraphDatabaseService db = DB.get();
		Index<Node> index = db.index().forNodes(INDEX_DATA_SOURCE);
		Transaction tx = db.beginTx();
		try {
			for (Node node : index.query(PROPERTY_USER_ID, "*")) {
				DataSource dataSource = nodeToDataSource(node);
				dataSources.add(dataSource);
			}
			
	        tx.success();
		}
		finally {
		    tx.finish();
		}
		
		return dataSources;
	}
	
	/**
	 * Convert a DataSource object from a Node
	 * @param node
	 * @return
	 */
	private DataSource nodeToDataSource(Node node) {
		DataSource dataSource = new DataSource();
		dataSource.setUserId   ((String) node.getProperty(PROPERTY_USER_ID, null));
		dataSource.setAgentUrl ((String) node.getProperty(PROPERTY_AGENT_URL, null));
		dataSource.setDataType ((String) node.getProperty(PROPERTY_DATA_TYPE, null));
		return dataSource;
	}

	/**
	 * Create a Node from given DataSource object
	 * @param db
	 * @param dataSource
	 * @return Node
	 */
	private Node dataSourceToNode(GraphDatabaseService db, DataSource dataSource) {
		Node node = db.createNode();
		node.setProperty(PROPERTY_USER_ID, dataSource.getUserId());
		node.setProperty(PROPERTY_AGENT_URL, dataSource.getAgentUrl());
		node.setProperty(PROPERTY_DATA_TYPE, dataSource.getDataType());
		return node;
	}

	@Override
	public String getDescription() {
		return "MerlinAgent can be used to find agents" +
				"providing specific user data such as contacts, state, " +
				"or history. Agents offering data can register themselves " +
				"to the MerlinAgent.";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}
	
    private static final String INDEX_DATA_SOURCE = "dataSource";
    private static final String PROPERTY_USER_ID = "userId";
    private static final String PROPERTY_AGENT_URL = "agentUrl";
    private static final String PROPERTY_DATA_TYPE = "dataType";    
}
