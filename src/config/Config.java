package config;
public class Config {
    private String password;
    private int durationMinutes;
    private boolean restrictTime;

    public Config(String password, int durationMinutes, boolean restrictTime) {
        this.password = password;
        this.durationMinutes = durationMinutes;
        this.restrictTime = restrictTime;
    }

    public String getPassword() {
        return password;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public boolean isRestrictTime() {
        return restrictTime;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setRestrictTime(boolean restrictTime) {
        this.restrictTime = restrictTime;
    }
}
