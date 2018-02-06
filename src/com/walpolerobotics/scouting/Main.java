package com.walpolerobotics.scouting;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    // api access code - base64 encoded username:authorizationkey;
    public static String accessCode = "change me";
    public String season = "2017";
    public String eventCode = "RIPRO";

    public ArrayList<MatchData> matchDataList = new ArrayList<MatchData>();

    public static void main(String[] args) {
	// write your code here
        new Main().getDataFromAPI();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getDataFromAPI() {
        // get list of all events
        getEventList(season);
        //getAttending("CMP-GALILEO");
        getSchedule(eventCode);

        printSchedule();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void printSchedule() {

        // create output file
        BufferedWriter output = null;
        String text = new String();
        try {
            String userDir = System.getProperty("user.home");
            String dataSheetDir = userDir + File.separator + "Documents" + File.separator + "Datasheets";
            System.out.println("Write Schedule to " + userDir + File.separator + "Documents" + File.separator + "Datasheets");

            File file = new File(dataSheetDir + File.separator + "FRC" + season + "_" + eventCode + ".csv");
            output = new BufferedWriter(new FileWriter(file));

            text = "Start Time" + "," + "Match #" + "," +
                    "Red1" + "," +
                    "Red2" + "," +
                    "Red3" + "," +
                    "Blue1" +"," +
                    "Blue2" +"," +
                    "Blue3" +"," +
                    "\n";
            output.write(text);

            for (MatchData md : matchDataList) {
                String outputText = md.startTime + "," + md.matchNumber + "," +
                        md.red1Number + "," +
                        md.red2Number + "," +
                        md.red3Number + "," +
                        md.blue1Number + "," +
                        md.blue2Number + "," +
                        md.blue3Number + ",";
                output.write(outputText + "\n");
            }

            output.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getAttending(String eventCode) {
        URL url;

        System.out.println("Get Teams Attending " + eventCode);

        String queryString = "https://frc-api.firstinspires.org/v2.0/";
        queryString += season + "/teams?";
        queryString += "eventCode=" + eventCode;

        try {
            url = new URL(queryString);

            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + accessCode);
                connection.setRequestProperty("Accept", "application/xml ");

                // get data returned
                String responseString = "";
                try {
                    BufferedReader br =
                            new BufferedReader((new InputStreamReader(connection.getInputStream())));

                    String input;
                    while ((input = br.readLine()) != null) {
                        //System.out.println(input);
                        responseString += input;
                    }
                    br.close();

                    // parse the xml file returned
                    DocumentBuilderFactory dbFactory
                            = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        try {
                            InputSource is = new InputSource(new StringReader(responseString));
                            Document doc = dBuilder.parse(is);
                            doc.getDocumentElement().normalize();

                            NodeList teamList = doc.getElementsByTagName("Team");
                            for (int c = 0; c < teamList.getLength(); c++) {
                                Node eNode = teamList.item(c);
                                if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) eNode;

                                    Integer teamNumber = Integer.parseInt(eElement.getElementsByTagName("teamNumber").item(0).getTextContent());
                                }
                            }

                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getSchedule(String eventCode) {
        URL url;

        System.out.println("Get Schedule for: " + eventCode);

        // example:
        //https://frc-api.firstinspires.org/v2.0/season/schedule/CMD-GALILEO?TournamentLevel=qual

        String queryString = "https://frc-api.firstinspires.org/v2.0/";
        queryString += season + "/schedule/";
        queryString += eventCode + "?TournamentLevel=qual";

        try {
            url = new URL(queryString);

            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + accessCode);
                connection.setRequestProperty("Accept", "application/xml ");

                // get data returned
                String responseString = "";
                try {
                    BufferedReader br =
                            new BufferedReader((new InputStreamReader(connection.getInputStream())));

                    String input;
                    while ((input = br.readLine()) != null) {
                        //System.out.println(input);
                        responseString += input;
                    }
                    br.close();

                    // parse the xml file returned
                    DocumentBuilderFactory dbFactory
                            = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        try {
                            InputSource is = new InputSource(new StringReader(responseString));
                            Document doc = dBuilder.parse(is);
                            doc.getDocumentElement().normalize();

                            NodeList matchList = doc.getElementsByTagName("ScheduledMatch");
                            for (int c = 0; c < matchList.getLength(); c++) {
                                Node matchNode = matchList.item(c);
                                if (matchNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) matchNode;
                                    Integer matchNumber = Integer.parseInt(eElement.getElementsByTagName("matchNumber").item(0).getTextContent());
                                    String startTimeOrig = eElement.getElementsByTagName("startTime").item(0).getTextContent();
                                    String startTime = "";

                                    DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    fromFormat.setLenient(false);
                                    DateFormat toFormat = new SimpleDateFormat("EEE hh:mm aa");
                                    toFormat.setLenient(false);
                                    try {
                                        Date date = fromFormat.parse(startTimeOrig);
                                        startTime = toFormat.format(date);
                                    } catch (java.text.ParseException e) {
                                        e.printStackTrace();
                                    }
                                    // convert start Time to Day of Week Time of Day (12 hour format)

                                    //System.out.println("Match: " + matchNumber);

                                    MatchData md = new MatchData(startTime,matchNumber);

                                    NodeList teamsNodeList = ((Element) matchNode).getElementsByTagName("Teams");
                                    for (int c2 = 0; c2 < teamsNodeList.getLength(); c2++) {
                                        Node teamsNode = teamsNodeList.item(c2);
                                        if (teamsNode.getNodeType() == Node.ELEMENT_NODE) {
                                            NodeList teamList = ((Element) teamsNode).getElementsByTagName("Team");
                                            //System.out.println("number of team nodes: " + teamList.getLength());
                                            for (int c3 = 0; c3 < teamList.getLength(); c3++) {
                                                Node teamNode = teamList.item(c3);
                                                if (teamNode.getNodeType() == Node.ELEMENT_NODE) {

                                                    Element teamElement = (Element) teamNode;
                                                    Integer teamNumber = Integer.parseInt(teamElement.getElementsByTagName("teamNumber").item(0).getTextContent());
                                                    String teamStation = teamElement.getElementsByTagName("station").item(0).getTextContent();

                                                    //System.out.println("Team: " + teamNumber + " Station: " + teamStation);
                                                    switch (teamStation) {
                                                        case "Red1" :
                                                            md.red1Number = teamNumber;
                                                            break;
                                                        case "Red2" :
                                                            md.red2Number = teamNumber;
                                                            break;
                                                        case "Red3" :
                                                            md.red3Number = teamNumber;
                                                            break;
                                                        case "Blue1" :
                                                            md.blue1Number = teamNumber;
                                                            break;
                                                        case "Blue2" :
                                                            md.blue2Number = teamNumber;
                                                            break;
                                                        case "Blue3" :
                                                            md.blue3Number = teamNumber;
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    matchDataList.add(md);
                                }
                            }

                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getEventList(String season) {
        URL url;
        String eventQueryString = "https://frc-api.firstinspires.org/v2.0/";
        eventQueryString += season;
        //eventQueryString += "/events?eventCode=" + eventCode;
        eventQueryString += "/events?";

        try {
            url = new URL(eventQueryString);

            try {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Basic " + accessCode);
                connection.setRequestProperty("Accept", "application/xml ");

                // get data returned
                String responseString = "";
                System.out.println("*** Event List ***");
                try {
                    BufferedReader br =
                            new BufferedReader((new InputStreamReader(connection.getInputStream())));

                    String input;
                    while ((input = br.readLine()) != null) {
                        //System.out.println("Event: " + input);
                        responseString += input;
                    }
                    br.close();

                    // parse the xml file returned
                    DocumentBuilderFactory dbFactory
                            = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        try {
                            InputSource is = new InputSource(new StringReader(responseString));
                            Document doc = dBuilder.parse(is);
                            doc.getDocumentElement().normalize();

                            NodeList eventList = doc.getElementsByTagName("Event");
                            for (int c = 0; c < eventList.getLength(); c++) {
                                Node eNode = eventList.item(c);
                                if (eNode.getNodeType() == Node.ELEMENT_NODE) {

                                    Element eElement = (Element) eNode;
                                    System.out.println("\nCode : " + eElement.getElementsByTagName("code").item(0).getTextContent());
                                    //System.out.println("City : " + eElement.getElementsByTagName("city").item(0).getTextContent());
                                    //System.out.println("Start : " + eElement.getElementsByTagName("dateStart").item(0).getTextContent());
                                    //System.out.println("End : " + eElement.getElementsByTagName("dateEnd").item(0).getTextContent());

                                }
                            }

                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
