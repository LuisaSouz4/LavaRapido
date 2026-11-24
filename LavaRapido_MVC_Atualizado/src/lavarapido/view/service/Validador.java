package lavarapido.service;

import java.util.regex.Pattern;

/** Validações e formatações de campos. */
public final class Validador {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.%+-]+@[\\w-]+(\\.[\\w-]+)+$");
    // Padrão antigo (ABC1234) e Mercosul (ABC1D23)
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    private Validador() { }

    public static String somenteDigitos(String s) { return s == null ? "" : s.replaceAll("\\D", ""); }

    public static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) return false;
        for (int t = 9; t < 11; t++) {
            int soma = 0;
            for (int i = 0; i < t; i++) soma += (d.charAt(i) - '0') * (t + 1 - i);
            int dv = (soma * 10) % 11;
            if (dv == 10) dv = 0;
            if (dv != d.charAt(t) - '0') return false;
        }
        return true;
    }

    public static boolean emailValido(String email) { return email != null && EMAIL.matcher(email.trim()).matches(); }

    public static boolean telefoneValido(String tel) {
        int n = somenteDigitos(tel).length();
        return n == 10 || n == 11;
    }

    public static String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public static boolean placaValida(String placa) { return PLACA.matcher(normalizarPlaca(placa)).matches(); }

    public static String formatarCpf(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11) return cpf;
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    public static String formatarTelefone(String tel) {
        String d = somenteDigitos(tel);
        if (d.length() == 11) return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        if (d.length() == 10) return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        return tel;
    }
}
