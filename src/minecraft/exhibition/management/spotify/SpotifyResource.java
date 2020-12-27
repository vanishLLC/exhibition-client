package exhibition.management.spotify;

public class SpotifyResource
{
    private String name;
    private String uri;
    private Location location;

    public SpotifyResource() {}

    public SpotifyResource(String name, String uri, Location location)
    {
        this.name = name;
        this.uri = uri;
        this.location = location;
    }

    public String getName()
    {
        return this.name;
    }

    public String getId()
    {
        if ((this.uri == null) || (!this.uri.startsWith("spotify:"))) {
            return null;
        }
        String[] split = this.uri.split(":");
        if (split.length != 3) {
            return null;
        }
        return split[2];
    }

    public String getUri()
    {
        return this.uri;
    }

    public Location getLocation()
    {
        return this.location;
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        SpotifyResource that = (SpotifyResource)o;
        if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
            return false;
        }
        if (this.uri != null ? !this.uri.equals(that.uri) : that.uri != null) {
            return false;
        }
        return that.location == null ? true : this.location != null ? this.location.equals(that.location) : false;
    }

    public int hashCode()
    {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.uri != null ? this.uri.hashCode() : 0);
        result = 31 * result + (this.location != null ? this.location.hashCode() : 0);
        return result;
    }

    public class Location
    {
        private String og;

        public Location() {}

        public String getOg()
        {
            return this.og;
        }

        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            Location location = (Location)o;

            return location.og == null ? true : this.og != null ? this.og.equals(location.og) : false;
        }

        public int hashCode()
        {
            return this.og != null ? this.og.hashCode() : 0;
        }
    }
}