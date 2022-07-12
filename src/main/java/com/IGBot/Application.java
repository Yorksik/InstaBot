package com.IGBot;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.requests.users.UsersSearchRequest;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import com.github.instagram4j.instagram4j.utils.IGUtils;
import kotlin.Pair;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class Application {

    static Logger log = Logger.getLogger(Application.class.getName());

    public static void main(String args[]) throws IGLoginException {
        String login = "natatalkova3";
        String password = "123456Aa";

        BasicConfigurator.configure(); // configure basic logging output
        Pair<String, String> pair = args.length >= 2 ? new Pair<>(args[0], args[1]) :
                                                        new Pair<>(login, password);
        //IGClient client = login(pair.getFirst(), pair.getSecond());
        IGClient client = twoFactorLogin(login, password);

        System.out.printf("Logged into %s\n", client.getSelfProfile().getUsername());
        new Application(client).start();

         System.exit(0);
    }

    private final IGClient client;

    public Application(IGClient client) {
        this.client = client;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Saving session...");
                SerializeUtil.serialize(client.getHttpClient().cookieJar(), new File("igcookies.ser"));
                SerializeUtil.serialize(client, new File("igclient.ser"));
                System.out.println("Saved!");
            }
        });
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.print("Input a command (Type 'quit' to quit): ");
        while (!(input = scanner.nextLine()).equalsIgnoreCase("quit")) {
            switch(input) {
                case "live":
                    new LiveBroadcastProcess(client, scanner).start();
                    break;
                default:
                    System.out.println("Unknown command. Available: live");
                    break;
            }

            System.out.print("Input a command (Type 'quit' to quit): ");
        }

    }


    public static IGClient twoFactorLogin(String login, String password) throws IGLoginException {

        BasicConfigurator.configure();

        Scanner scanner = new Scanner(System.in);

        // Callable that returns inputted code from System.in
        Callable<String> inputCode = () -> {
            System.out.print("Please input code: ");
            return scanner.nextLine();
        };

        // handler for two factor login
        IGClient.Builder.LoginHandler twoFactorHandler = (client, response) -> {
            // included utility to resolve two factor
            // may specify retries. default is 3
            return IGChallengeUtils.resolveTwoFactor(client, response, inputCode);
        };

        IGClient client = IGClient.builder()
                .username(login)
                .password(password)
                .onTwoFactor(twoFactorHandler)
                .login();

        SerializeUtil.serialize(client.getHttpClient().cookieJar(), new File("igcookies.ser"));
        SerializeUtil.serialize(client, new File("igclient.ser"));

        System.out.printf("Logged into %s\n", client.getSelfProfile().getUsername());

        return client;
    }
    public static IGClient login(String username, String password) throws IGLoginException {
        System.out.printf("Logging into %s\n", username);
        IGClient client = getLoggedInIGClient(username, password);
        //System.out.println("Serializing IGClient and cookies");
        log.info("Serializing IGClient and cookies");
        System.out.printf("Logged into %s\n", client.getSelfProfile().getUsername());

        // save session
        SerializeUtil.serialize(client.getHttpClient().cookieJar(), new File("igcookies.ser"));
        SerializeUtil.serialize(client, new File("igclient.ser"));

        return client;
    }

    public static boolean validSerializedLogin(IGClient client) {
        // return true if any requests are successful
        // return false if any request are not (CompletionException arises due to invalid login)
        return CompletableFuture.anyOf(client.actions().simulate().postLoginFlow().toArray(new CompletableFuture[15]))
                .handle((o, tr) -> tr == null)
                .join();
    }

    public static IGClient getLoggedInIGClient(String username, String password) throws IGLoginException {
        File serializedClient = new File("igclient.ser"),
                serializedCookies = new File("igcookies.ser");

        if (serializedClient.exists() && serializedCookies.exists()) {
            //System.out.println("Found existing serialized info.");
            log.info("Found existing serialized info.");
            try {
                IGClient deserialized_client = SerializeUtil.getClientFromSerialize(serializedClient, serializedCookies);

                if (validSerializedLogin(deserialized_client)) {
                    System.out.println("Logged into saved session.");
                    return deserialized_client;
                } else {
                    System.out.println("Invalid saved session.");
                }
            } catch (Exception e) {}
        }

        System.out.println("Creating a new IGClient");

        return IGClient.builder()
                .username(username)
                .password(password)
                .client(IGUtils.defaultHttpClientBuilder().cookieJar(new SerializableCookieJar()).build())
                .simulatedLogin();
                //.login();
    }
}
