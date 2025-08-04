import java.io.*;
import java.net.*;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class server {
    public static void main(String[] args) throws Exception {
        // num puerto
        int puerto = 6789;
        // socket escucha
        ServerSocket escuchasock = new ServerSocket(puerto);
        // cuadrar el threadpool
        ExecutorService threadpool = Executors.newFixedThreadPool(10);
        System.out.println("server is connected to the port " + puerto);
        System.out.println("ctrl + c to close");

        // QUIero que el server esté siempre act
        while (true) {
            Socket siempreACT = escuchasock.accept();
            // corro threadpool
            try {
                threadpool.execute(new SoliHTTP(siempreACT));
            } catch (Exception e) {
                System.out.println("error, yuca" + e);
            }
        }
    }
} // server lo hice

final class SoliHTTP implements Runnable {
    // para leer linea vacía en Linux y/o Windows
    final static String GRFL = "\r\n";
    Socket cliSocket;

    public SoliHTTP(Socket cliSocket) throws Exception {
        this.cliSocket = cliSocket;
    }

    @Override
    public void run() {
        try {
            proccessReq();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void proccessReq() throws Exception {
        // LO que le mando al server
        DataOutputStream out = new DataOutputStream(cliSocket.getOutputStream());
        // Para leer HEaders y peticiones
        BufferedReader br = new BufferedReader(new InputStreamReader(cliSocket.getInputStream()));

        // Pedir LInea
        String requestLine = br.readLine();
        System.out.println(requestLine);

        // leer headers
        String headeString = null;
        // en efecto hay header
        while ((headeString = br.readLine()) != null && headeString.length() != 0) {
            System.out.println(headeString);
        }
        StringTokenizer partLine = new StringTokenizer(requestLine); // acá va lo primero que leímos
        String hmeth = partLine.nextToken();
        String archivo = partLine.nextToken();

        // quiero encontrar el nombre del index
        if (archivo.equals("/")) {
            archivo = "index.html";
        } else {
            archivo = archivo.startsWith("/") ? archivo.substring(1) : archivo; // entender esto, pero busco que el archivo del requestline sea igual al INdex
        }
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(archivo);
        } catch (Exception e) {
            fileExists = false;
        }
        String estadoPetition;
        String contenidoPetition;
        String bodyMessage = "";
        if (fileExists == true) {
            estadoPetition = "HTTP/1.0 200 OK" + GRFL;
            contenidoPetition = "Content-Type:"+contentType(archivo) + GRFL;
        }else{  
            estadoPetition = "HTTP/1.0 404 Not Found" + GRFL;
            contenidoPetition = "content-Type:"+contentType(archivo) + GRFL;
            bodyMessage= "<HTML>" + 
                "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                "<BODY><b>404</b> Not Found</BODY></HTML>";;
        }
        //envía la línea de estado
        out.writeBytes(estadoPetition);
        //envía el contenido de la línea de content type
        out.writeBytes(contenidoPetition);
        //cierra la conexión
        out.writeBytes("Connection: close"+GRFL);
        //envía línea vacía para indicar el final de las líneas de encabezado
        out.writeBytes(GRFL);

        if (fileExists == true) {
            // sendBytes
            sendBytes(fis, out);
            fis.close();
        }else{
            out.writeBytes(bodyMessage);
        }
        //después de responder cierro el socket
        cliSocket.close();
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
{
   // Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket.
   byte[] buffer = new byte[1024];
   int bytes = 0;

   // Copia el archivo solicitado hacia el output stream del socket.
   while((bytes = fis.read(buffer)) != -1 ) {
      os.write(buffer, 0, bytes);
   }
}
    private static String contentType(String nombreArchivo)
{
        if(nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
                return "text/html";
        }
        if(nombreArchivo.endsWith(".gif")) {
                return"image/gif";
        }
        if(nombreArchivo.endsWith(".jpg")) {
                return "image/jpeg";
        }
        return "application/octet-stream";
}


}