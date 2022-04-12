package me.farin.solana;

public class Utils {


    public static String parse(String responsebody){
        String[] strings = responsebody.split(",");
        for(String s : strings){
            if(s.contains("floorPrice")){
                String[] split = s.split(":");
                return split[1];
            }
        }

        return responsebody;
    }
}
