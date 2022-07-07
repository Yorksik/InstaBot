package com.IGBot;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.requests.accounts.AccountsSetBiographyRequest;
import com.github.instagram4j.instagram4j.requests.commerce.CommerceDestinationRequest;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsFeedsRequest;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.requests.users.UsersSearchRequest;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;
import okhttp3.OkHttpClient;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws IGLoginException {
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
                .username("natatalkova2")
                .password("123456Aa")
                .onTwoFactor(twoFactorHandler)
                .login();
/*        IGClient client = IGClient.builder()
                .username("natatalkova3")
                .password("123456Aa")
                .login();*/
        UsersSearchRequest usersSearchRequest = new UsersSearchRequest("kiraaa3945");
        System.out.println(usersSearchRequest.toString());

        FeedUserRequest feedUserRequest = new FeedUserRequest(client.getSelfProfile().getPk());
        System.out.println(feedUserRequest);


/*        InstagramSearchUsernameResult userResult = client.sendRequest(
                new InstagramSearchUsernameRequest("katy001122"));
        System.out.println(userResult.getUser().username);

        // получение информации о пользователе
        InstagramSearchUsernameResult userResult2 = client.sendRequest(
                new InstagramSearchUsernameRequest("kiraaa3945"));

       *//* // получение подписчиков
        InstagramGetUserFollowersResult followersResult = client.sendRequest(
                new InstagramGetUserFollowersRequest(userResult.getUser().getPk()));
        for (InstagramUserSummary user : followersResult.getUsers()) {
            System.out.println(user.full_name + " " + user.pk);
        }*//*

        InstagramGetUserFollowersResult followersResult2 = client.sendRequest(
                new InstagramGetUserFollowersRequest(userResult2.getUser().getPk()));
        int i = 0;

        for (InstagramUserSummary user : followersResult2.getUsers()) {
            i++;
            sleep(500);
            System.out.println(user.getUsername() + " " + i);
        }*/
    }
}