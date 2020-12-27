package exhibition.management.spotify;

import com.google.gson.annotations.SerializedName;

public class SpotifyTrack
{
    @SerializedName("track_resource")
    private SpotifyResource trackInformation;
    @SerializedName("artist_resource")
    private SpotifyResource artistInformation;
    @SerializedName("album_resource")
    private SpotifyResource albumInformation;
    private int length;
    private String image;

    public SpotifyTrack() {}

    public SpotifyTrack(SpotifyResource trackInformation, SpotifyResource artistInformation, SpotifyResource albumInformation, int length)
    {
        this.trackInformation = trackInformation;
        this.artistInformation = artistInformation;
        this.albumInformation = albumInformation;
        this.length = length;
    }

    public SpotifyResource getTrackInformation()
    {
        return this.trackInformation;
    }

    public SpotifyResource getArtistInformation()
    {
        return this.artistInformation;
    }

    public SpotifyResource getAlbumInformation()
    {
        return this.albumInformation;
    }

    public int getLength()
    {
        return this.length;
    }

    public String getImage()
    {
        return this.image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public boolean hasTrackInformation()
    {
        return (getTrackInformation() != null) && (getAlbumInformation() != null) && (getArtistInformation() != null);
    }

    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        SpotifyTrack that = (SpotifyTrack)o;
        if (this.length != that.length) {
            return false;
        }
        if (this.trackInformation != null ? !this.trackInformation.equals(that.trackInformation) : that.trackInformation != null) {
            return false;
        }
        if (this.artistInformation != null ? !this.artistInformation.equals(that.artistInformation) : that.artistInformation != null) {
            return false;
        }
        if (this.albumInformation != null ? !this.albumInformation.equals(that.albumInformation) : that.albumInformation != null) {
            return false;
        }
        return false;
    }

    public int hashCode()
    {
        int result = this.trackInformation != null ? this.trackInformation.hashCode() : 0;
        result = 31 * result + (this.artistInformation != null ? this.artistInformation.hashCode() : 0);
        result = 31 * result + (this.albumInformation != null ? this.albumInformation.hashCode() : 0);
        result = 31 * result + this.length;
        return result;
    }

}

