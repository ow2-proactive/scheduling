/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s):
*
* ################################################################
*/
package org.objectweb.proactive.core.body;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.event.MessageEventListener;
import org.objectweb.proactive.core.mop.MethodCall;


public class HalfBody extends AbstractBody {

	//
	// -- PRIVATE MEMBERS -----------------------------------------------
	//
	
	private static final String HALF_BODY_EXCEPTION_MESSAGE = "This method is not implemented in class HalfBody.";

	private static final String NAME = "Other thread";
	/** The component in charge of receiving reply */
	private ReplyReceiver replyReceiver;

	public synchronized static HalfBody getHalfBody(MetaObjectFactory factory) {
		return new HalfBody(factory);
	}

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	private HalfBody(MetaObjectFactory factory) {
		super(new Object(), "LOCAL", factory);
		this.replyReceiver = factory.newReplyReceiverFactory().newReplyReceiver();
		setLocalBodyImpl(new HalfLocalBodyStrategy(factory.newRequestFactory()));
		this.localBodyStrategy.getFuturePool().setOwnerBody(this.getID());
	}

	//
	// -- PUBLIC METHODS -----------------------------------------------
	//

	//
	// -- implements MessageEventProducer -----------------------------------------------
	//

	public void addMessageEventListener(MessageEventListener listener) {
	}

	public void removeMessageEventListener(MessageEventListener listener) {
	}

	//
	// -- PROTECTED METHODS -----------------------------------------------
	//

	/**
	 * Receives a request for later processing. The call to this method is non blocking
	 * unless the body cannot temporary receive the request.
	 * @param request the request to process
	 * @exception java.io.IOException if the request cannot be accepted
	 */
	protected void internalReceiveRequest(Request request) throws java.io.IOException {
		throw new ProActiveRuntimeException("The method 'receiveRequest' is not implemented in class HalfBody.");
	}

	/**
	 * Receives a reply in response to a former request.
	 * @param reply the reply received
	 * @exception java.io.IOException if the reply cannot be accepted
	 */
	protected void internalReceiveReply(Reply reply) throws java.io.IOException {

		replyReceiver.receiveReply(reply, this, getFuturePool());
	}

	//
	// -- inner classes -----------------------------------------------
	//

	private class HalfLocalBodyStrategy implements LocalBodyStrategy, java.io.Serializable {

		/** A pool future that contains the pending future objects */
		protected FuturePool futures;

		protected RequestFactory internalRequestFactory;

		private long absoluteSequenceID;

		//
		// -- CONSTRUCTORS -----------------------------------------------
		//

		public HalfLocalBodyStrategy(RequestFactory requestFactory) {
			this.futures = new FuturePool();
			this.internalRequestFactory = requestFactory;
		}

		//
		// -- PUBLIC METHODS -----------------------------------------------
		//

		//
		// -- implements LocalBody -----------------------------------------------
		//

		public FuturePool getFuturePool() {
			return futures;
		}

		public BlockingRequestQueue getRequestQueue() {
			throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
		}
		
		public RequestQueue getHighPriorityRequestQueue() {
			throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
		}

		public Object getReifiedObject() {
			throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
		}

		public String getName() {
			return NAME;
		}

		public void serve(Request request) {
			throw new ProActiveRuntimeException(HALF_BODY_EXCEPTION_MESSAGE);
		}

		public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody) throws java.io.IOException {
			long sequenceID = getNextSequenceID();
			Request request = internalRequestFactory.newRequest(methodCall, HalfBody.this, future == null, sequenceID);
			if (future != null) {
				future.setID(sequenceID);
				futures.receiveFuture(sequenceID, future.getCreatorID(), future);
			}
			request.send(destinationBody);
		}

		//
		// -- PROTECTED METHODS -----------------------------------------------
		//

		/**
		* Returns a unique identifier that can be used to tag a future, a request
		* @return a unique identifier that can be used to tag a future, a request.
		*/
		private synchronized long getNextSequenceID() {
			return ++absoluteSequenceID;
		}

	} // end inner class LocalHalfBody
}
