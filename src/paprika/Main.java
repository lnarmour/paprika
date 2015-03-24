package paprika;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import paprika.analyzer.Analyzer;
import paprika.analyzer.SootAnalyzer;
import paprika.neo4j.ModelToGraph;
import paprika.neo4j.QueryEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Created by Geoffrey Hecht on 19/05/14.
 */

public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static String computeSha256(String path) throws IOException, NoSuchAlgorithmException {
        byte[] buffer = new byte[2048];
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = new FileInputStream(path)) {
            while (true) {
                int readBytes = is.read(buffer);
                if (readBytes > 0)
                    digest.update(buffer, 0, readBytes);
                else
                    break;
            }
        }
        byte[] hashValue = digest.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashValue.length; i++) {
            sb.append(Integer.toString((hashValue[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika").description("Collect metrics from apk.");
        parser.addArgument("apk").help("Path of the APK to analyze");
        parser.addArgument("-a","--androidJars").required(true).help("Path to android platforms jars");
        parser.addArgument("-db","--database").required(true).help("Path to neo4J Database folder");
        parser.addArgument("-n","--name").required(true).help("Name of the application");
        parser.addArgument("-p","--package").required(true).help("Application main package");
        parser.addArgument("-k","--key").required(true).help("sha256 of the apk used as identifier");
        parser.addArgument("-dev","--developer").required(true).help("Application developer");
        parser.addArgument("-cat","--category").required(true).help("Application category");
        parser.addArgument("-nd","--nbDownload").required(true).help("Numbers of downloads for the app");
        parser.addArgument("-d","--date").required(true).help("Date of download");
        parser.addArgument("-r","--rating").type(Double.class).required(true).help("application rating");
        parser.addArgument("-pr","--price").setDefault("Free").help("Price of the application");
        parser.addArgument("-s","--size").type(Integer.class).required(true).help("Size of the application");
        parser.addArgument("-u","--unsafe").help("Unsafe mode (no args checking)");
        parser.addArgument("-vc","--versionCode").setDefault("").help("Version Code of the application (extract from manifest)");
        parser.addArgument("-vn","--versionName").setDefault("").help("Version Name of the application (extract from manifest)");
        parser.addArgument("-tsdk","--targetSdkVersion").setDefault("").help("Target SDK Version (extract from manifest)");
        parser.addArgument("-sdk","--sdkVersion").setDefault("").help("sdk version (extract from manifest)");
        try {
            Namespace res = parser.parseArgs(args);
            //queryMode(res);
            if(res.get("unsafe") == null){
                checkArgs(res);
            }
            runAnalysis(res);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkArgs(Namespace arg) throws Exception{
        String sha256 = computeSha256(arg.getString("apk"));
        if(!sha256.equals(arg.getString("key").toLowerCase())){
            throw new Exception("The given key is different from sha256 of the apk");
        }
        if(!arg.getString("date").matches("^([0-9]{4})-([0-1][0-9])-([0-3][0-9])\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]*)$")){
            throw new Exception("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
        }
    }

    public static void runAnalysis(Namespace arg) throws Exception {
        System.out.println("Collecting metrics");
        Analyzer analyzer = new SootAnalyzer(arg.getString("apk"),arg.getString("androidJars"),
                arg.getString("name"),arg.getString("key").toLowerCase(),
                arg.getString("package"),arg.getString("date"),arg.getInt("size"),
                arg.getString("developer"),arg.getString("category"),arg.getString("price"),
                arg.getDouble("rating"),arg.getString("nbDownload"),arg.getString("versionCode"),arg.getString("versionName"),arg.getString("sdkVersion"),arg.getString("targetSdkVersion"));
        analyzer.init();
        analyzer.runAnalysis();
        System.out.println("Saving into database "+arg.getString("database"));
        ModelToGraph modelToGraph = new ModelToGraph(arg.getString("database"));
        modelToGraph.insertApp(analyzer.getPaprikaApp());
        System.out.println("Done");
    }

    public static void queryMode(Namespace arg) throws Exception {
        System.out.println("Executing Queries");
        QueryEngine queryEngine = new QueryEngine(arg.getString("database"));
        queryEngine.MIMQuery();
    }
}