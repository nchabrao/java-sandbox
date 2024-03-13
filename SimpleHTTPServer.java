import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHTTPServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/agenda", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) {
   
            String requestMethod = t.getRequestMethod();
            String query = t.getRequestURI().getQuery();
            int hpId = Integer.parseInt(query.substring(query.indexOf('=')+1,query.indexOf('&')));
            StringBuilder response = new StringBuilder();
            response.append("<b>Agenda checker</b></br>Method: " + requestMethod + "</br>" + "Query: " + query + "</br>"+"Health professional identifier: " + hpId + "</br>");
            query = query.substring(query.indexOf('&')+1);
            String password = query.substring(query.indexOf("=")+1);
            
            Agenda agenda = new Agenda();
            PostgreClient client = new PostgreClient(password);

            OutputStream os = null;
            try{
                response.append(Agenda.listSlots(agenda.getNextFreeSlots(client.getSession(), hpId)));
                t.sendResponseHeaders(200, response.length());
                os = t.getResponseBody();
                os.write(response.toString().getBytes());
            }
            catch(IOException e){
                System.out.println("An I/O exception occured when sending response: "+e.getMessage());
                e.printStackTrace();
            } catch (SQLTimeoutException e) {
                System.out.println("An SQL timeout exception occured when sending response: "+e.getMessage());
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("A Database access exception occured when sending response: "+e.getMessage());
                e.printStackTrace();
            }
            finally{
                if (os != null){
                    try{
                        os.close();
                    }
                    catch(IOException e){
                        System.out.println("An I/O exception occured when closing output stream: "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}