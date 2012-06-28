import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Servlet for processing WebSocket connections.
 * 
 * @author <a href="mailto:azatsarynnyy@exoplatform.org">Artem Zatsarynnyy</a>
 * @version $Id: EchoWebSocketServlet.java Jun 5, 2012 2:57:40 AM azatsarynnyy $
 *
 */
public class EchoWebSocketServlet extends WebSocketServlet
{
   private static final long serialVersionUID = 1L;

   /**
    * Create the instance that will process inbound connection.
    * 
    * @param subProtocol the sub-protocol agreed between the client and server or null if none was agreed
    * @see org.apache.catalina.websocket.WebSocketServlet#createWebSocketInbound(java.lang.String)
    */
   @Override
   protected StreamInbound createWebSocketInbound(String subProtocol)
   {
      return new EchoStreamInbound();
   }

   /**
    * Class used to process WebSocket connections.
    */
   private static final class EchoStreamInbound extends StreamInbound
   {
      /**
       * Simply echo the data to back to the client.
       * This method is called when there is a binary WebSocket message available to process.
       * 
       * @param is the WebSocket message
       * @throws IOException if a problem occurs processing the message.
       *         Any exception will trigger the closing of the WebSocket connection.
       * @see org.apache.catalina.websocket.StreamInbound#onBinaryData(java.io.InputStream)
       */
      @Override
      protected void onBinaryData(InputStream is) throws IOException
      {
         // Obtain the outbound side of this WebSocket connection used for writing data to the client
         WsOutbound outbound = getWsOutbound();

         int data = is.read();
         while (data != -1)
         {
            outbound.writeBinaryData(data);
            data = is.read();
         }

         outbound.flush();
      }

      /**
       * Simply echo the data to back to the client.
       * This method is called when there is a textual WebSocket message available to process.
       * 
       * @param reader the WebSocket message
       * @throws IOException if a problem occurs processing the message.
       *         Any exception will trigger the closing of the WebSocket connection.
       * @see org.apache.catalina.websocket.StreamInbound#onTextData(java.io.Reader)
       */
      @Override
      protected void onTextData(Reader reader) throws IOException
      {
         // Obtain the outbound side of this WebSocket connection used for writing data to the client
         WsOutbound outbound = getWsOutbound();

         int character = reader.read();
         while (character != -1)
         {
            outbound.writeTextData((char)character);
            character = reader.read();
         }

         outbound.flush();
      }
   }

}
