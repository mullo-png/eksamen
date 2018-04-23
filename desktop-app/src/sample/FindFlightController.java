package sample;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.LocalDateStringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import sample.model.Airport;
import sample.model.Flight;
import sample.model.Passenger;
import sample.model.RespFromAPI;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by coag on 23-04-2018.
 */
public class FindFlightController {
    @FXML
    private TextField inputFrom;
    @FXML
    private TextField inputTo;
    @FXML
    private DatePicker inputDate;

    @FXML
    private TableView tbl;

    @FXML
    private Label labelStatus;

    private Airport selectedFrom;
    private Airport selectedTo;

    public void findFrom(KeyEvent keyEvent) {
        findAirports(inputFrom, true);
    }

    public void findTo(KeyEvent keyEvent) {
        findAirports(inputTo, false);
    }

    public void findFlights(ActionEvent actionEvent) {
        int fromAirportId = selectedFrom.getId();
        int toAirportId = selectedTo.getId();
        LocalDate date = inputDate.getValue();
        System.out.println("------FIND---");

        System.out.println("from: " + fromAirportId);
        System.out.println("to: " + toAirportId);
        System.out.println("date: " + date);

        try {
            URL url = new URL(MyUtil.API2_URL +
                    "&action=get" +
                    "&items=flights" +
                    "&date=" + date +
                    "&from=" + fromAirportId +
                    "&to=" + toAirportId
            );
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            InputStream resp = con.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(resp);
            Scanner sc = new Scanner(resp);
            String data = sc.nextLine();

            //byte[] data = new byte[1024];
            //resp.read(data);

            System.out.println("-----Resp------ " + url);

            String respString = new String(data).trim();

            System.out.println(respString);

            JsonArray respJsonArray = new JsonParser().parse(respString.trim()).getAsJsonArray();

            System.out.println("---GSON---");
            System.out.println(respJsonArray.toString());

            Gson g = new Gson();
            JsonElement element0 = respJsonArray.get(0);
            RespFromAPI rfa = g.fromJson(element0, RespFromAPI.class);
            System.out.println(rfa);
            labelStatus.setText(rfa.toString());

            Iterator<JsonElement> it = respJsonArray.iterator();
            it.next(); // first element is the status code
            List<Flight> airports = new ArrayList<>();
            while (it.hasNext()) {
                JsonElement element = it.next();
                Flight flight = g.fromJson(element, Flight.class);
                airports.add(flight);
                System.out.println(flight);


            }

            List<String> tt = new ArrayList<String>();
            tt.add("flg no");
            tt.add("dep");
            tt.add("arr");
            tt.add("pla");
            tt.add("gate");

            tbl.setItems(FXCollections.observableList(tt));



            System.out.println(airports);


        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void findAirports(TextField textFieldToSuggestCompletion, boolean isFrom) {
        String srcWord = textFieldToSuggestCompletion.getText();
        if (srcWord.length() >= 2 && srcWord.length() <= 8) {
            System.out.println(srcWord);

            try {
                URL url = new URL(MyUtil.API_URL +
                        "&action=get" +
                        "&items=airport" +
                        "&search=" + srcWord);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                InputStream resp = con.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(resp);
                Scanner sc = new Scanner(resp);
                String data = sc.nextLine();

                //byte[] data = new byte[1024];
                //resp.read(data);

                System.out.println("-----Resp------ " + url);

                String respString = new String(data).trim();

                System.out.println(respString);

                JsonArray respJsonArray = new JsonParser().parse(respString.trim()).getAsJsonArray();

                System.out.println("---GSON---");
                System.out.println(respJsonArray.toString());

                Gson g = new Gson();
                JsonElement element0 = respJsonArray.get(0);
                RespFromAPI rfa = g.fromJson(element0, RespFromAPI.class);
                System.out.println(rfa);
                labelStatus.setText(rfa.toString());

                Iterator<JsonElement> it = respJsonArray.iterator();
                it.next(); // first element is the status code
                List<Airport> airports = new ArrayList<Airport>();
                while (it.hasNext()) {
                    JsonElement element = it.next();
                    Airport airport = g.fromJson(element, Airport.class);
                    airports.add(airport);
                    System.out.println(airport);
                }

                AutoCompletionBinding<Airport> bind = TextFields.bindAutoCompletion(textFieldToSuggestCompletion, airports);
                bind.setOnAutoCompleted(new EventHandler<AutoCompletionBinding.AutoCompletionEvent<Airport>>() {
                    @Override
                    public void handle(AutoCompletionBinding.AutoCompletionEvent<Airport> event) {
                        if (isFrom) {
                            selectedFrom = event.getCompletion();
                        } else {
                            selectedTo = event.getCompletion();
                        }
                    }
                });


            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }

    }


}
