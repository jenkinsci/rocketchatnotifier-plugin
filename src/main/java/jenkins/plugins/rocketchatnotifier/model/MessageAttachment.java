package jenkins.plugins.rocketchatnotifier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import jenkins.plugins.rocketchatnotifier.utils.IgnoreInheritedIntrospector;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ben on 07/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageAttachment extends AbstractDescribableImpl<MessageAttachment> {

  private String color;
  private String text;
  private String ts;

  @JsonProperty("thumb_url")
  private String thumbUrl;
  @JsonProperty("message_link")
  private String messageLink;
  private Boolean collapsed;
  @JsonProperty("author_name")
  private String authorName;
  private final String title;

  @JsonProperty("author_link")
  private String authorLink;
  @JsonProperty("author_icon")
  private String authorIcon;
  @JsonProperty("title_link")
  private String titleLink;
  @JsonProperty("title_link_download")
  private Boolean titleLinkDownload;
  @JsonProperty("image_url")
  private String imageUrl;
  @JsonProperty("audio_url")
  private String audioUrl;
  @JsonProperty("video_url")
  private String videoUrl;

  @DataBoundConstructor
  public MessageAttachment(String title) {
    this.title = title;
  }

  public String getColor() {
    return color;
  }

  @DataBoundSetter
  public void setColor(final String color) {
    this.color = color;
  }

  public String getText() {
    return text;
  }

  @DataBoundSetter
  public void setText(final String text) {
    this.text = text;
  }

  public String getTs() {
    return ts;
  }

  @DataBoundSetter
  public void setTs(final String ts) {
    this.ts = ts;
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  @DataBoundSetter
  public void setThumbUrl(final String thumbUrl) {
    this.thumbUrl = thumbUrl;
  }

  public String getMessageLink() {
    return messageLink;
  }

  @DataBoundSetter
  public void setMessageLink(final String messageLink) {
    this.messageLink = messageLink;
  }

  @DataBoundSetter
  public void setCollapsed(final Boolean collapsed) {
    if (collapsed) {
      this.collapsed = collapsed;
    }
  }

  public String getAuthorName() {
    return authorName;
  }

  @DataBoundSetter
  public void setAuthorName(final String authorName) {
    this.authorName = authorName;
  }

  public String getTitle() {
    return title;
  }

  public Boolean getCollapsed() {
    return collapsed;
  }

  public String getAuthorLink() {
    return authorLink;
  }

  public void setAuthorLink(final String authorLink) {
    this.authorLink = authorLink;
  }

  public String getAuthorIcon() {
    return authorIcon;
  }

  @DataBoundSetter
  public void setAuthorIcon(final String authorIcon) {
    this.authorIcon = authorIcon;
  }

  public String getTitleLink() {
    return titleLink;
  }

  @DataBoundSetter
  public void setTitleLink(final String titleLink) {
    this.titleLink = titleLink;
  }

  public Boolean getTitleLinkDownload() {
    return titleLinkDownload;
  }

  @DataBoundSetter
  public void setTitleLinkDownload(final Boolean titleLinkDownload) {
    if (titleLinkDownload) {
      this.titleLinkDownload = titleLinkDownload;
    }
  }

  public String getImageUrl() {
    return imageUrl;
  }

  @DataBoundSetter
  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getAudioUrl() {
    return audioUrl;
  }

  @DataBoundSetter
  public void setAudioUrl(final String audioUrl) {
    this.audioUrl = audioUrl;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  @DataBoundSetter
  public void setVideoUrl(final String videoUrl) {
    this.videoUrl = videoUrl;
  }


  public static MessageAttachment fromJSON(final JSONObject json) {
    final MessageAttachment attachment = new MessageAttachment(json.optString("title"));
    attachment.setColor(sanitizeEmptyStringtoNull(json.getString("color")));
    attachment.setText(sanitizeEmptyStringtoNull(json.getString("text")));
    attachment.setThumbUrl(sanitizeEmptyStringtoNull(json.getString("thumbUrl")));
    attachment.setMessageLink(sanitizeEmptyStringtoNull(json.getString("messageLink")));
    attachment.setCollapsed(json.getBoolean("collapsed"));
    attachment.setAuthorName(sanitizeEmptyStringtoNull(json.getString("authorName")));
    attachment.setAuthorIcon(sanitizeEmptyStringtoNull(json.getString("authorIcon")));
    attachment.setAuthorLink(sanitizeEmptyStringtoNull(json.getString("authorLink")));
    attachment.setTitleLink(sanitizeEmptyStringtoNull(json.getString("titleLink")));
    attachment.setTitleLinkDownload(json.getBoolean("titleLinkDownload"));
    attachment.setImageUrl(sanitizeEmptyStringtoNull(json.getString("imageUrl")));
    attachment.setAudioUrl(sanitizeEmptyStringtoNull(json.getString("audioUrl")));
    attachment.setVideoUrl(sanitizeEmptyStringtoNull(json.getString("videoUrl")));
    return attachment;
  }

  private static String sanitizeEmptyStringtoNull(String text) {
    return text == null || text.length() == 0 ? null : text;
  }

  public static List<Map<String, Object>> convertMessageAttachmentsToMaps(List<MessageAttachment> attachments) {
    List<Map<String, Object>> returnedList = new ArrayList<Map<String, Object>>();
    if (attachments != null && attachments.size() > 0) {
      ObjectMapper oMapper = new ObjectMapper();
      oMapper.setAnnotationIntrospector(new IgnoreInheritedIntrospector());
      for (MessageAttachment attachment : attachments) {
        returnedList.add(oMapper.convertValue(attachment, Map.class));
      }
    }
    return returnedList;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageAttachment that = (MessageAttachment) o;
    return Objects.equals(color, that.color) &&
      Objects.equals(text, that.text) &&
      Objects.equals(ts, that.ts) &&
      Objects.equals(thumbUrl, that.thumbUrl) &&
      Objects.equals(messageLink, that.messageLink) &&
      Objects.equals(collapsed, that.collapsed) &&
      Objects.equals(authorName, that.authorName) &&
      Objects.equals(title, that.title) &&
      Objects.equals(authorLink, that.authorLink) &&
      Objects.equals(authorIcon, that.authorIcon) &&
      Objects.equals(titleLink, that.titleLink) &&
      Objects.equals(titleLinkDownload, that.titleLinkDownload) &&
      Objects.equals(imageUrl, that.imageUrl) &&
      Objects.equals(audioUrl, that.audioUrl) &&
      Objects.equals(videoUrl, that.videoUrl);
  }

  @Override
  public int hashCode() {

    return Objects.hash(color, text, ts, thumbUrl, messageLink, collapsed, authorName, title, authorLink, authorIcon, titleLink, titleLinkDownload, imageUrl, audioUrl, videoUrl);
  }

  @Extension
  @JsonIgnoreType
  public static class DescriptorImpl extends Descriptor<MessageAttachment> {
    public String getDisplayName() {
      return "";
    }
  }
}
