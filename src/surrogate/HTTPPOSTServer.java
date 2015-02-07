package surrogate;

import java.io.*;
import java.net.*;
import java.util.*;

import sun.net.www.protocol.http.HttpURLConnection;

public class HTTPPOSTServer extends Thread {

	static final String HTML_START = "<html>" + "<title>HTTP POST Server in java</title>"
			+ "<body>";

	static final String HTML_END = "</body>" + "</html>";
	
	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;

	private Model model;

	public HTTPPOSTServer(Socket client, Model model) {
		this.model = model;
		connectedClient = client;
	}

	static String babble(Model model) throws Exception {
		Integer length = ((int) (Math.random() * 10)) + 5;
		Vector<Token> tokens = new Vector<Token>();
		Token p2 = new Token("<s>", "<s>");
		Token p1 = new Token("<s>", "<s>");
		Token p;
		for (int i = 0; i < length; i++) {
			p = model.predict(p2, p1);
			if (p.word.equals("<end>")) {
				break;
			}
			tokens.add(p);
			p2 = p1;
			p1 = p;
		}

		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(System.out));
		try {
			for (int i = 0; i < tokens.size(); i++) {
				wr.write(tokens.get(i).word + " ");
			}
			wr.write("\n");
			wr.flush();
			String sentence = tokens.get(0).word;
			for (int i = 1; i < tokens.size(); i++) {
				sentence += " " + tokens.get(i).word;
			}
			sentence += ".";
			return sentence;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "BAD";
	}

	public void sendPost(String urlParameters, String url, String sentence) throws Exception {
		URL obj = new URL("http://" + url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		urlParameters += "&response=" + sentence;
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
	}
	
	public void run() {

		String currentLine = null;

		try {

			System.out.println("The Client " + connectedClient.getInetAddress() + ":"
					+ connectedClient.getPort() + " is connected");

			inFromClient = new BufferedReader(new InputStreamReader(
					connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());

			currentLine = inFromClient.readLine();
			String headerLine = currentLine;
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			if (httpMethod.equals("GET")) {
				System.out.println("GET request");
				String sentence = babble(this.model);
				if (httpQueryString.equals("/test")) {
					sendResponse(200, sentence, false);
					return;
				}
				String keys = httpQueryString.split("\\?")[1];
				String[] params = keys.split("&");
				String decoded = null;
				String url = null;
				for (String param : params) {
					String key = param.split("=")[0];	
					String value = URLDecoder.decode(param.split("=")[1], "UTF-8");
					if (decoded == null) {
						decoded = key + "=" + value;
					} else {
						decoded += "&" + key + "=" + value;
					}
					if (key.equals("reply_to")) {
						url = value;
					}	
				}
				sendPost(decoded, url, "lol");
				sendPost(decoded, url, sentence);
				System.out.println("Done sending replies.");
				sendResponse(200, sentence, false);
			} else {
				System.out.println("POST request");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendResponse(int statusCode, String responseString, boolean isFile)
			throws Exception {

		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String fileName = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;

		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

		if (isFile) {
			fileName = responseString;
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";
		} else {
			responseString = HTTPPOSTServer.HTML_START + responseString + HTTPPOSTServer.HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		}

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");

		outToClient.writeBytes(responseString);

		outToClient.close();
	}

}
