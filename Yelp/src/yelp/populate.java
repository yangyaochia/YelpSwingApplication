package hw3;
/**
 */
import org.json.*;
import java.sql.*;
import java.io.*;
import static java.sql.JDBCType.CLOB;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Timestamp;
import static java.sql.Types.CLOB;
import java.text.DateFormat;
import java.util.Date;
import oracle.jdbc.OracleConnection;
import static oracle.jdbc.OracleTypes.CLOB;
import oracle.sql.CLOB;
import oracle.jdbc.OraclePreparedStatement;

class Business {
    String business_id;
    String city;
    String state;
    String name;
    String full_address;
    double stars;
    Business(String business_id, String city, String state, String name, String full_address, double stars) {
        this.business_id = business_id;
        this.city = city;
        this.state = state;
        this.name = name;
        this.full_address = full_address;
        this.stars = stars;
    }
}
class OpenHour {
    String business_id;
    String day;
    String from_time;
    String to_time;
    OpenHour(String business_id, String day, String from_time, String to_time) {
        this.business_id = business_id;
        this.day = day;
        this.from_time = from_time;
        this.to_time = to_time;
    }
}

class MainCategory {
    String business_id;
    String mainCategory;
    MainCategory(String business_id, String mainCategory) {
        this.business_id = business_id;
        this.mainCategory = mainCategory;
    }
}

class SubCategory {
    String business_id;
    String subcategory;
    SubCategory(String business_id, String subcategory) {
        this.business_id = business_id;
        this.subcategory = subcategory;
    }
}


class Attribute {
    String business_id;
    String attribute;
    Attribute(String business_id, String attribute) {
        this.business_id = business_id;
        this.attribute = attribute;
    }
}

public class populate {

    private final String DB_USER = "system";
    private final String DB_PASS = "coen280hw3";
    private final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    private final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private String BUSINESS_JSON_FILE_PATH;
    private String USER_JSON_FILE_PATH;
    private String REVIEW_JSON_FILE_PATH;
    private String CHECKIN_JSON_FILE_PATH;// = "C:\\Users\\wendi\\Desktop\\hw3\\json_files\\yelp_review_half.json";
    
    private Connection con;
    
    public List<Business> businesses = new ArrayList();
    public List<MainCategory> mainCategories = new ArrayList();
    public List<SubCategory> subCategories = new ArrayList();
    public List<Attribute> attributes = new ArrayList();
    public List<OpenHour> openHours = new ArrayList();
    public HashSet<String> mainCategoriesHash = new HashSet();
    public HashMap<String,Integer> checkinHash = new HashMap<String, Integer>();

    public void run(String args[]) throws IOException {
        try {
            BUSINESS_JSON_FILE_PATH = "yelp_business.json";//args[0];
            REVIEW_JSON_FILE_PATH = "yelp_review.json";//args[1];
            CHECKIN_JSON_FILE_PATH = "yelp_checkin.json";//args[2];
            USER_JSON_FILE_PATH = "yelp_user.json";//args[3];
            con = getConnect();
            System.out.println("Connection successful");
            init();            
            parseCheckin();
            insertBusinessFileData();
            parseAndInsertYelpUser();
            parseAndInsertReview();
            
        } catch (SQLException ex) {
            Logger.getLogger(populate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(populate.class.getName()).log(Level.SEVERE, null, ex);
        } /*catch (IOException ex) {
            Logger.getLogger(populate.class.getName()).log(Level.SEVERE, null, ex);
        } */finally {
            closeConnection(con);
        }
    }
    
    
    private void init() throws SQLException, ClassNotFoundException {
        System.out.println("+++++++++++++ START initialization +++++++++++++");
        addMainCategories();
        cleanTable();
        
   

        System.out.println("------------- END initialization -------------");
    }
    private void addMainCategories() {
        System.out.println("Set MainCategories...");
        mainCategoriesHash.add("Active Life");
        mainCategoriesHash.add("Arts & Entertainment");
        mainCategoriesHash.add("Automotive");
        mainCategoriesHash.add("Car Rental");
        mainCategoriesHash.add("Cafes");
        mainCategoriesHash.add("Beauty & Spas");
        mainCategoriesHash.add("Convenience Stores");
        mainCategoriesHash.add("Dentists");
        mainCategoriesHash.add("Doctors");
        mainCategoriesHash.add("Drugstores");
        mainCategoriesHash.add("Department Stores");
        mainCategoriesHash.add("Education");
        mainCategoriesHash.add("Event Planning & Services");
        mainCategoriesHash.add("Flowers & Gifts");
        mainCategoriesHash.add("Food");
        mainCategoriesHash.add("Health & Medical");
        mainCategoriesHash.add("Home Services");
        mainCategoriesHash.add("Home & Garden");
        mainCategoriesHash.add("Hospitals");
        mainCategoriesHash.add("Hotels & Travel");
        mainCategoriesHash.add("Hardware Stores");
        mainCategoriesHash.add("Grocery");
        mainCategoriesHash.add("Medical Centers");
        mainCategoriesHash.add("Nurseries & Gardening");
        mainCategoriesHash.add("Nightlife");
        mainCategoriesHash.add("Restaurants");
        mainCategoriesHash.add("Shopping");
        mainCategoriesHash.add("Transportation");
    }
    
    private void cleanTable() throws SQLException, ClassNotFoundException {
        
            System.out.println("Clean Business table...");
            Statement statement = con.createStatement();
            statement.executeUpdate("DELETE FROM Business");
            statement.close();


            System.out.println("Clean Attribute table...");
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM Attribute");
            statement.close();
            
            System.out.println("Clean SubCategory table...");
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM SubCategory");

            statement.close();
            
            System.out.println("Clean MainCategory table...");
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM MainCategory");  
            statement.close();

            System.out.println("Clean OpenHour table...");
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM OpenHour");  
            statement.close();
            
            System.out.println("Clean YelpUser table...");
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM YelpUser");
            statement.close();
            
            System.out.println("Clean Review table...");
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM Review");
            statement.close();

            

        /*} finally {
            closeConnection(connection);
        }*/
    }
    private Connection getConnect() throws SQLException, ClassNotFoundException{
        System.out.println("Checking JDBC...");
        Class.forName(JDBC_DRIVER);
        System.out.println("Connecting to database...");
        return DriverManager.getConnection(ORACLE_URL, DB_USER, DB_PASS);
    }
    private void closeConnection(Connection con) {
        try {
            con.close();
            System.out.println("Disconnection Successful.");
        } catch (SQLException e) {
            System.err.println("Cannot close connection");
        } 
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void parseCheckin() {
        File file = new File(CHECKIN_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
                ){//Connection connection = getConnect();){
            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject obj = new JSONObject(line);
                String business_id = obj.getString("business_id");
                Integer count = 0;
                JSONObject attributes1 = obj.getJSONObject("checkin_info");
                Iterator<?> keys1 = attributes1.keys();
                while (keys1.hasNext()) {
                    String key1 = (String) keys1.next();
                    count += (Integer) attributes1.get(key1); 
                }
                if ( checkinHash.containsKey(business_id) ) {
                    checkinHash.put(business_id, checkinHash.get(business_id) + count);
                } else {
                    checkinHash.put(business_id, count);
                }
                //System.out.println(business_id+" "+checkinHash.get(business_id));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }    
    
    private void parseBusiness() {
        File file = new File(BUSINESS_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
                ){//Connection connection = getConnect();){
            String line;
            JSONObject obj;
            String business_id;
            String city;
            String state;
            String name;
            String full_address;
            Double stars;
            JSONArray arr;
            while ((line = reader.readLine()) != null) {
                obj = new JSONObject(line);
                business_id = obj.getString("business_id");
                city = obj.getString("city");
                state = obj.getString("state");
                name = obj.getString("name");
                full_address = obj.getString("full_address");
                stars = obj.getDouble("stars");
                arr = obj.getJSONArray("categories");
                for (int i = 0; i < arr.length(); i++) {
                    String category = arr.getString(i);
                    if (mainCategoriesHash.contains(category)) {
                        mainCategories.add(new MainCategory(business_id, category));
                    }
                    else {
                        subCategories.add(new SubCategory(business_id, category));
                    }
                }
                //System.out.println(business_id);
                businesses.add(new Business(business_id, city, state, name, full_address, stars));
                
                JSONObject attributes1 = obj.getJSONObject("attributes");
                Iterator<?> keys1 = attributes1.keys();
                while (keys1.hasNext()) {
                    String key1 = (String) keys1.next();
                    StringBuilder sb1 = new StringBuilder(key1);
                    if (attributes1.get(key1) instanceof JSONObject) {
                        JSONObject attributes2 = attributes1.getJSONObject(key1);
                        Iterator<?> keys2 = attributes2.keys();
                        while (keys2.hasNext()) {
                            String key2 = (String) keys2.next();
                            StringBuilder sb2 = new StringBuilder(key2);
                            sb2.append("_");
                            sb2.append(attributes2.get(key2));
                            //System.out.println(sb1.toString() + "_" + sb2.toString() );
                            attributes.add(new Attribute(business_id, sb1.toString() + "_" + sb2.toString()));
                        }
                    }
                    else {
                        sb1.append("_");
                        sb1.append(attributes1.get(key1));
                        attributes.add(new Attribute(business_id, sb1.toString()));
                    }
                }
                
                JSONObject hours1 = obj.getJSONObject("hours");
                Iterator<?> hourskeys1 = hours1.keys();
                String to_time = null, from_time = null;
                while (hourskeys1.hasNext()) {
                    String key1 = (String) hourskeys1.next();
                    //StringBuilder sb1 = new StringBuilder(hourskeys1);
                    
                    if (hours1.get(key1) instanceof JSONObject) {
                        JSONObject hours2 = hours1.getJSONObject(key1);
                        to_time = hours2.getString("close");
                        from_time = hours2.getString("open");                        
                    }
                    //System.out.println(key1 + " " + from_time +" " + to_time);
                    /*SimpleDateFormat dateFormat;
                    Date parsedFrom, parsedTo;
                    Timestamp timeFrom = null, timeTo = null;
                    try {
                        dateFormat = new SimpleDateFormat("HH:mm");
                        parsedFrom = dateFormat.parse(from_time);
                        parsedTo = dateFormat.parse(to_time);
                        timeFrom = new java.sql.Timestamp(parsedFrom.getTime());
                        timeTo = new java.sql.Timestamp(parsedTo.getTime());
                    } catch(ParseException e) { //this generic but you can control another types of exception
                        // look the origin of excption 
                    }*/
                    //System.out.println(timeFrom +" " +timeTo);
                    openHours.add(new OpenHour(business_id, key1, from_time, to_time));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    private void parseAndInsertReview() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing review.json file and inserting data into Review table...");
        File file = new File(REVIEW_JSON_FILE_PATH);
        //OracleConnection conn = (OracleConnection)getConnect();
        //Clob clob = conn.createClob();     
//        File file = new File(REVIEW_JSON_FILE_HALF_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
                ){//Connection connection = getConnect();) {
            String line;
            String sql = "INSERT /* + APPEND */ INTO Review (review_id, business_id, user_id, review_date, review_text, stars, votes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (OraclePreparedStatement preparedStatement = (OraclePreparedStatement) con.prepareStatement(sql)) {
                JSONObject obj;
                String review_id;
                String business_id;
                String user_id;
                String review_date;
                String review_text;
                int stars;
                int votes;
                while ((line = reader.readLine()) != null) {
                    // parse review data
                    obj = new JSONObject(line);
                    review_id = obj.getString("review_id");
                    business_id = obj.getString("business_id");
                    user_id = obj.getString("user_id");
                    review_date = obj.getString("date");
                    review_text = obj.getString("text");
                    stars = obj.getInt("stars");
                    votes = obj.getJSONObject("votes").getInt("useful");
                    //clob.setString( 1, review_text);
                    // insert review data
                    //System.out.println(clob);
                    preparedStatement.setString(1, review_id);
                    preparedStatement.setString(2, business_id);
                    preparedStatement.setString(3, user_id);
                    preparedStatement.setString(4, review_date);
                    preparedStatement.setStringForClob(5, review_text);
                    preparedStatement.setInt(6, stars);
                    preparedStatement.setInt(7, votes);
                    preparedStatement.executeUpdate();
                    
                }
            }
        }
    }
    
    private void parseAndInsertYelpUser() throws IOException, SQLException, ClassNotFoundException {
        System.out.println("Parsing user.json file and inserting data into YelpUser table...");
        File file = new File(USER_JSON_FILE_PATH);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader);
                ){//Connection connection = getConnect();) {
            String line;
            String sql = "INSERT /* + APPEND */ INTO YelpUser (user_id, name, yelping_since, review_count, average_stars, friend_count, votes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            //try () {
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                //try () {
                JSONObject obj;
                String user_id;
                String name;
                String yelping_since;
                int review_count;
                double average_stars;
                int friend_count;
                int votes;
                while ((line = reader.readLine()) != null) {
                    // parse user data
                    obj = new JSONObject(line);
                    user_id = obj.getString("user_id");
                    name = obj.getString("name");
                    yelping_since = obj.getString("yelping_since");
                    review_count = obj.getInt("review_count");
                    average_stars = obj.getDouble("average_stars");
                    friend_count = obj.getJSONArray("friends").length();
                    votes = obj.getJSONObject("votes").getInt("useful")
                            + obj.getJSONObject("votes").getInt("funny")
                            + obj.getJSONObject("votes").getInt("cool");
                    // insert user data
                    
                    preparedStatement.setString(1, user_id);
                    preparedStatement.setString(2, name);
                    preparedStatement.setString(3, yelping_since);
                    preparedStatement.setInt(4, review_count);
                    preparedStatement.setDouble(5, average_stars);
                    preparedStatement.setInt(6, friend_count);
                    preparedStatement.setInt(7, votes);
                    preparedStatement.executeUpdate();
                }
                //} finally {
                //    connection.close();
                //}
                //}
            }
        }
    }
    
    /**
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void insertBusinessFileData() throws SQLException {//throws SQLException, ClassNotFoundException {
        //try (Connection connection = getConnect()){
            System.out.println("Creating statement...");

            System.out.println("Parsing Business.json file...");
            parseBusiness();

            String sql;
            PreparedStatement preparedStatement;


            
            System.out.println("Insert data into Business table...");
            sql = "INSERT /* + APPEND */ INTO Business (business_id, city, state, name, full_address, stars, number_checkin) VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = con.prepareStatement(sql);
            //System.out.println("businesses size " + businesses.size());
            for (Business b: businesses) {
                preparedStatement.setString(1, b.business_id);
                preparedStatement.setString(2, b.city);
                preparedStatement.setString(3, b.state);
                preparedStatement.setString(4, b.name);
                preparedStatement.setString(5, b.full_address);
                preparedStatement.setDouble(6, b.stars);
                if ( checkinHash.containsKey(b.business_id)) {
                    preparedStatement.setInt(7, checkinHash.get(b.business_id));
                } else {
                    preparedStatement.setInt(7, 0);
                }
                //System.out.println(b.business_id);
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();

            System.out.println("Insert data into OpenHour table...");
            sql = "INSERT /* + APPEND */ INTO OpenHour (business_id, day, from_time, to_time) VALUES (?, ?, ?, ?)";
            preparedStatement = con.prepareStatement(sql);
            for (OpenHour a: openHours) {
                preparedStatement.setString(1, a.business_id);
                preparedStatement.setString(2, a.day);
                preparedStatement.setString(3, a.from_time);
                //System.out.println(a.to_time);
                //System.out.println(a.from_time);
                preparedStatement.setString(4, a.to_time);
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();            

            System.out.println("Insert data into MainCategory table...");
            sql = "INSERT /* + APPEND */ INTO MainCategory (business_id, mainCategory) VALUES (?, ?)";
            preparedStatement = con.prepareStatement(sql);
            for (MainCategory m: mainCategories) {
                preparedStatement.setString(1, m.business_id);
                preparedStatement.setString(2, m.mainCategory);
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();

            System.out.println("Insert data into SubCategory table...");
            sql = "INSERT /* + APPEND */ INTO SubCategory (business_id, subCategory) VALUES (?, ?)";
            preparedStatement = con.prepareStatement(sql);
            for (SubCategory s: subCategories) {
                preparedStatement.setString(1, s.business_id);
                preparedStatement.setString(2, s.subcategory);
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
            
            System.out.println("Insert data into Attribute table...");
            sql = "INSERT /* + APPEND */ INTO Attribute (business_id, attribute) VALUES (?, ?)";
            preparedStatement = con.prepareStatement(sql);
            for (Attribute a: attributes) {
                preparedStatement.setString(1, a.business_id);
                preparedStatement.setString(2, a.attribute);
                preparedStatement.executeUpdate();
            }

        //}
    }

    
    public static void main(String[] args) throws IOException {
        System.out.println("Populate JSON data into Database."); 
        populate p = new populate();
        p.run(args);
    }
}
