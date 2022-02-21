public class Utils {


    public static String parse(String responsebody){
        String[] strings = responsebody.split(",");
        for(String s : strings){
            if(s.contains("floorPrice")){
                String[] split = s.split(":");
                return format(split[1]);
            }
        }

        return responsebody;
    }

    public static String format(String string){
        String newSting = null;
        if(string.length() == 10){
            String a = String.valueOf(string.charAt(0));
            String b = String.valueOf(string.charAt(1));
            String c = String.valueOf(string.charAt(2));

            newSting = a + "."+b+c;
        } else if(string.length() == 11){
            String a = String.valueOf(string.charAt(0));
            String b = String.valueOf(string.charAt(1));
            String c = String.valueOf(string.charAt(2));
            String d = String.valueOf(string.charAt(3));

            newSting = a+b + "."+c +d;

        } else if(string.length() == 12){
            String a = String.valueOf(string.charAt(0));
            String b = String.valueOf(string.charAt(1));
            String c = String.valueOf(string.charAt(2));
            String d = String.valueOf(string.charAt(3));
            String f = String.valueOf(string.charAt(4));


            newSting = a+b +c +"." +d +f;

        }



        else {
            String a = String.valueOf(string.charAt(0));
            String b = String.valueOf(string.charAt(1));

            newSting = "0." +a+b;
        }

        return newSting;
    }
}
