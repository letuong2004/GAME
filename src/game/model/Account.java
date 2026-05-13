package game.model;

public class Account {
    private final String username;
    private final String password;
    private final String nickname;
    private final String avatar;
    private int wins;
    private int losses;
    private int points;
    private boolean online;

    public Account(String username, String password, String nickname, String avatar) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.wins = 0;
        this.losses = 0;
        this.points = 1000;
        this.online = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getPoints() {
        return points;
    }

    public void addWin() {
        wins++;
        points += 20;
    }

    public void addLoss() {
        losses++;
        points = Math.max(0, points - 10);
    }

    public void addDraw() {
        points += 5;
    }

    public String getWinRate() {
        int total = wins + losses;
        if (total == 0) {
            return "0%";
        }
        return String.format("%d%%", Math.round((wins * 100f) / total));
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - W:%d L:%d Rate:%s Points:%d", nickname, username, wins, losses, getWinRate(), points);
    }
}
