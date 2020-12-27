package exhibition.gui.altmanager;

import exhibition.Client;

public final class Alt {

    public enum Status {

        Working("\247aWorking"), TempBan("\2476Temp Banned"), Banned("\247cBanned"), Unchecked("\247eUnchecked"), NotWorking("\2474Not Working");

        Status(String string) {
            this.formatted = string;
        }

        private final String formatted;

        public String toFormatted() {
            return this.formatted;
        }

    }

    private String mask;
    private final String username;
    private String password;
    private Status status;
    private long unbanDate = -1;

    public Alt(final String username, final String password) {
        this(username, password, Status.Unchecked);
    }

    public Alt(final String username, final String password, Status status) {
        this(username, password, "", status, -1);
    }

    public Alt(final String username, final String password, String mask, Status status) {
        this(username, password, mask, status, -1);
    }

    public Alt(final String username, final String password, final String mask, Status status, long unbanDate) {
        this.username = username;
        this.password = password;
        this.mask = mask;
        this.status = status;
        this.unbanDate = unbanDate;
    }

    public String getMask() {
        return this.mask == null ? "" : this.mask;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public long getUnbanDate() {
        return this.unbanDate;
    }

    public void setMask(final String mask) {
        this.mask = mask;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUnbanDate(final long unbanDate) {
        this.unbanDate = unbanDate;
    }

    public boolean hasTimePassed() {
        return this.unbanDate != -1 && (this.unbanDate <= System.currentTimeMillis());
    }

    public boolean isUnbanned() {
        return this.status != Alt.Status.Banned && this.status != Alt.Status.TempBan && this.status != Alt.Status.NotWorking;
    }

    public boolean isGenerated() {
        return !this.username.contains("@alt.com");
    }

    public boolean isAltening() {
        return this.username.contains("@alt.com");
    }

    public boolean isValid() {
        return !Client.altService.isVanilla() ? isAltening() : isGenerated();
    }

}

