package de.psi.paip.mes.frontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import de.psi.paip.mes.frontend.messages.Message;
import de.psi.paip.mes.frontend.messages.MessageDispatcher;
import de.psi.paip.mes.frontend.messages.MessageType;
import de.psi.paip.mes.frontend.messages.SessionInformation;
import de.psi.paip.mes.frontend.model.workflow.Payload;
import de.psi.paip.mes.frontend.model.workflow.UserTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.BroadcastStateTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshAllNeighboursTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.ServiceTask;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Component
public class WorkflowWebSocketHandler extends TextWebSocketHandler {

	@Getter
	@Setter
	private String terminalId = "";

	@Getter
	@Setter
	private String uniqueIdentifier = "";

	private static final Logger LOG = LoggerFactory.getLogger(WorkflowWebSocketHandler.class);

	private final ObjectMapper mapper;

	private List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<WebSocketSession>());

	private final MessageDispatcher messageDispatcher;

	@Value("${reconnect.timerInterval}")
	private String reconnectTimer;

	@Value("${app.version}")
	private String appVersion;


	public WorkflowWebSocketHandler(ObjectMapper mapper, MessageDispatcher messageDispatcher) {
		this.mapper = mapper;
		this.messageDispatcher = messageDispatcher;
	}

	/** just for testing can be removed
	 *
	 * @param sessionId Id of the websocket-session to be closed
	 * @return success / failure of closing a connection
	 */
	public boolean closeSession(String sessionId){
			Optional<WebSocketSession> session = sessions.stream().filter(s -> s.getId().equals(sessionId)).findFirst();
			if(session.isPresent()){
				try{
					session.get().close();
					LOG.info("Close Session:"+ session.get().getId()+" for Terminal:"+ session.get().getAttributes().get("terminalId").toString());
					return true;
				}catch(Exception e){
					LOG.trace(e.toString());
					return false;
				}
			}else{
				return false;
			}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		String uri= session.getUri().toString();
		Map<String, String> queryMap = this.parseUri(uri);

		String terminalId = queryMap.get("terminalId");
		String uniqueIdentifier = queryMap.get("uniqueIdentifier");

		session.getAttributes().put("terminalId", terminalId);
		session.getAttributes().put("uniqueIdentifier", uniqueIdentifier);

		LOG.info("Session {} established with session attributes {} ", session.getId(),
				session.getAttributes().toString());
		this.sessions.add(session);
		try {
			SessionInformation information = new SessionInformation(session.getId(), reconnectTimer, appVersion, uniqueIdentifier);
			session.sendMessage(new TextMessage(mapper.writeValueAsBytes(information)));
		} catch (IOException e) {
			LOG.error("Error sending message", e);
		}

		// sendOpenEntries(this.terminalId);
	}

	// parse URL params
	public Map<String, String> parseUri(String url){

		MultiValueMap<String, String> queryParams =
				UriComponentsBuilder.fromUriString(url).build().getQueryParams();

		return queryParams.toSingleValueMap();
	}

	/**
	 * Send open Entries to Queue
	 * @param terminalId Id of the terminal
	 * */
	public void sendOpenEntries(String terminalId){
		LOG.trace("Check open entries for Terminal:"+ terminalId);
		Iterator<Message> iterator = messageDispatcher.getBlockingQueue().iterator();

		while(iterator.hasNext()) {
			Message m = iterator.next();

			LOG.trace(m.toString());
			if(m.getTerminalId().equals(terminalId)){
				LOG.trace("Found open Message for Terminal");

				if(m.getType().equals(MessageType.serviceTask)){
					LOG.trace("Push ServiceTask to Terminal");
					pushServiceTask((ServiceTask) m.getActivity());
					messageDispatcher.deleteEntry(m);
				}else if(m.getType().equals(MessageType.userTask)){
					LOG.trace("Push UserTask to Terminal");
					try {
						final String jsonString = mapper.writeValueAsString(m.getActivity());

						JsonElement jsonTree = new JsonParser().parse(jsonString);

						pushWorkflowChange(jsonTree.getAsJsonObject());
						messageDispatcher.deleteEntry(m);
					} catch (JsonProcessingException e) {
						LOG.error("Unable to serialize user task: {}", e.getMessage());
					}

				}else{
					LOG.trace("No valid message type found");
				}
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		LOG.info("Session {} closed. ", session.getId());
		sessions.remove(session);
	}

	/** 
	 * Method to check whether a session is currently open
	 * 
	 * @param terminalId , terminalId of a terminal (Terminal.Class = businessKey)
	 * @return check if a websocket-session to a terminal exists
	 * */
	public boolean checkSessionIsOpen(String terminalId) {
		LOG.trace("check for session-terminalId:"+terminalId);

		Optional<WebSocketSession> session = this.sessions.stream().filter(t -> t.getAttributes().containsValue(terminalId)).findFirst();
		if(session.isPresent()){
			LOG.trace("terminal session for terminal "+terminalId+" is open");
			return true;
		}else{
			LOG.trace("terminal session for TerminalId:"+terminalId+" is not open, message will not be delivered");
			return false;
		}
	}

	/**
	 * Method to check whether a session is currently open
	 *
	 * @param uniqueIdentifier sessionId of the websocket session
	 * @return session requested websocket session
	 * */
	public Optional<WebSocketSession> getSession(String uniqueIdentifier) {
		Optional<WebSocketSession> session = this.sessions.stream().filter(t -> t.getAttributes().containsValue(uniqueIdentifier)).findFirst();
		return session;
	}

	/**
	 * sessionId is possibly not distinct if service-frontend is shut down and run again, because Clients still have the old id
	 * */
	private boolean checkTerminalId(Map<String, Object> attributes, String terminalId){
		return attributes.containsValue(terminalId);
	}



	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		super.handleTextMessage(session, message);
		LOG.info("WebSocket: " + message.getPayload());
	}

	/**
	 * not used now
	 * @param payload payload to be sent to gui
	 */
	public void pushWorkflowChange(Payload payload) {
		try {
			for (WebSocketSession session : new ArrayList<>(sessions)) {
				if (session.isOpen()) {
					session.sendMessage(new TextMessage(mapper.writeValueAsBytes(payload)));
				} else {
					LOG.error("Session is not open.");
				}
			}
		} catch (IOException ex) {
			LOG.error("oops", ex);
		}
	}

	/**
	 * @param activity UserTask to be sent to GUI
	 */
	public void pushWorkflowChange(UserTask activity) {
		try {
			for (WebSocketSession session : new ArrayList<>(sessions)) {
				if (session.isOpen()) {
					session.sendMessage(new TextMessage(mapper.writeValueAsBytes(activity)));
				} else {
					LOG.error("Session is not open.");
				}
			}
		} catch (IOException ex) {
			LOG.error("oops", ex);
		}
	}

	/**
	 * @param activity activity supposed to bes sent to gui
	 * */
	public void pushWorkflowChange(String activity) {
		LOG.info("activity-as-string");
		try {
			for (WebSocketSession session : new ArrayList<>(sessions)) {
				if (session.isOpen()) {

					JsonParser jsonParser = new JsonParser();

					JsonElement jsonTree = jsonParser.parse(activity);

					JsonObject jsonRoot = jsonTree.getAsJsonObject();

					LOG.info(activity);
					Gson gson = new Gson();

					session.sendMessage(new TextMessage(gson.toJson(jsonRoot)));

				} else {
					LOG.error("Session is not open.");
				}
			}
		} catch (IOException ex) {
			LOG.error("oops", ex);
		}
	}

	private static boolean validateWorkStation(JsonObject jsonObject) {
		final String VARIABLES = "variables";
		final String WORK_STATION = "workStation";

		if (jsonObject.has(VARIABLES) && jsonObject.get(VARIABLES) != null
				&& !(jsonObject.get(VARIABLES) instanceof JsonNull)) {
			final JsonObject variables = jsonObject.getAsJsonObject(VARIABLES);
			return variables.has(WORK_STATION) && variables.get(WORK_STATION) != null
					&& !(variables.get(WORK_STATION) instanceof JsonNull);
		}
		return false;
	}

	public void pushWorkflowChange(JsonObject activity) {
		LOG.info("activity-as-JsonObject");
		try {
			if (!validateWorkStation(activity)) {
				LOG.info("No variable workStation found. Ignoring...");
				return;
			}

			final JsonObject variables = activity.getAsJsonObject("variables");
			final String terminalId = variables.get("workStation").getAsString();

			if (!StringUtils.hasText(terminalId) || "null".equals(terminalId)) {
				LOG.info("No terminal ID in business key. Ignoring...");
				return;
			}

			LOG.info("Pushing activity with business key {} to GUI(s)...", terminalId);
			LOG.trace("Looking for recipients...");
			boolean found= false;
			for (WebSocketSession session : new ArrayList<>(sessions)) {
				String sessionTerminalId = session.getAttributes().get("terminalId").toString();
				//LOG.trace("Checking session {}...", sessionTerminalId);
				if (session.isOpen()) {
					//LOG.trace("sessionTerminalId: {}; terminalId: {}", sessionTerminalId, terminalId);
					if (sessionTerminalId.equals(terminalId)) {
						found=true;
						LOG.info("Sending {} to terminal {}...", activity.get("activityType"), terminalId);
						LOG.trace(">>> {}", activity);
						session.sendMessage(new TextMessage(new Gson().toJson(activity)));
					}

				} else {
					LOG.error("Session is not open.");
				}
			}
			if(found == false){
				LOG.error("No open Session found for Terminal"+terminalId);
			}
		} catch (IOException ex) {
			LOG.error("oops", ex);
		}
	}
	/**
	 * Method is used to send Service Tasks to frontend / GUI
	 * 
	 * @param task ServiceTask, can be one of the following: {@link BroadcastStateTask}, {@link RefreshAllNeighboursTask}, {@link RefreshTask}
	 * */
	public void pushServiceTask(ServiceTask task) {
		LOG.info("Sending service-task to GUI.");
		try {
			for (WebSocketSession session : new ArrayList<>(sessions)) {
				if (session.isOpen()) {

					try {
						String terminalId = task.getTerminalId();
						String sessionTerminalId = session.getAttributes().get("terminalId").toString();

						if (terminalId != null && !terminalId.equals("null") && !terminalId.equals("")) {

							// terminalId matches businesskey in event message
							if (sessionTerminalId.equals(task.getTerminalId())) {
								session.sendMessage(new TextMessage(mapper.writeValueAsString(task)));
							}
						}
					} catch (Exception e) {
						session.sendMessage(new TextMessage(mapper.writeValueAsString(task)));
					}
				} else {
					LOG.error("Session is not open.");
				}
			}
		} catch (IOException ex) {
			LOG.error("oops", ex);
		}
	}

}
