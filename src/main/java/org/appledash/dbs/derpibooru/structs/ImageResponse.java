package org.appledash.dbs.derpibooru.structs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * An image response from the Philomena API.
 * See https://derpibooru.org/pages/api for more information.
 * 
 * @param animated Whether the image is animated.
 * @param aspectRatio The image's width divided by its height.
 * @param commentCount The number of comments made on the image.
 * @param createdAt The creation time, in UTC, of the image.
 * @param deletionReason The hide reason for the image, or null if none provided. This will only have a value on images which are deleted for a rule violation.
 * @param description The image's description.
 * @param downvotes The number of downvotes the image has.
 * @param duplicateOf The ID of the target image, or null if none provided. This will only have a value on images which are merged into another image.
 * @param duration The number of seconds the image lasts, if animated, otherwise .04.
 * @param faves The number of faves the image has.
 * @param firstSeenAt The time, in UTC, the image was first seen (before any duplicate merging).
 * @param format The file extension of the image. One of "gif", "jpg", "jpeg", "png", "svg", "webm".
 * @param height The image's height, in pixels.
 * @param hiddenFromUsers Whether the image is hidden. An image is hidden if it is merged or deleted for a rule violation.
 * @param id The image's ID.
 * @param intensities Optional object of internal image intensity data for deduplication purposes. May be null if intensities have not yet been generated.
 * @param mimeType The MIME type of this image. One of "image/gif", "image/jpeg", "image/png", "image/svg+xml", "video/webm".
 * @param name The filename that the image was uploaded with.
 * @param origSha512Hash The SHA512 hash of the image as it was originally uploaded.
 * @param processed Whether the image has finished optimization.
 * @param representations A mapping of representation names to their respective URLs. Contains the keys "full", "large", "medium", "small", "tall", "thumb", "thumb_small", "thumb_tiny".
 * @param score The image's number of upvotes minus the image's number of downvotes.
 * @param sha512Hash The SHA512 hash of this image after it has been processed.
 * @param size The number of bytes the image's file contains.
 * @param sourceUrl The current source URL of the image.
 * @param spoilered Whether the image is hit by the current filter.
 * @param tagCount The number of tags present on the image.
 * @param tagIds A list of tag IDs the image contains.
 * @param tags A list of tag names the image contains.
 * @param thumbnailsGenerated Whether the image has finished thumbnail generation. Do not attempt to load images from view_url or representations if this is false.
 * @param updatedAt The time, in UTC, the image was last updated.
 * @param uploader The image's uploader.
 * @param uploaderId The ID of the image's uploader. null if uploaded anonymously.
 * @param upvotes The image's number of upvotes.
 * @param viewUrl The image's view URL, including tags.
 * @param width The image's width, in pixels.
 * @param wilsonScore The lower bound of the Wilson score interval for the image, based on its upvotes and downvotes, given a z-score corresponding to a confidence of 99.5%.
 */
@SuppressWarnings({"ClassWithTooManyFields", "NegativelyNamedBooleanVariable", "ClassWithTooManyMethods"})
@JsonIgnoreProperties(ignoreUnknown = true)
public final record ImageResponse(
        @JsonProperty("animated") boolean animated,
        @JsonProperty("aspect_ratio") float aspectRatio,
        @JsonProperty("comment_count") int commentCount,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("deletion_reason") String deletionReason,
        @JsonProperty("description") String description,
        @JsonProperty("downvotes") int downvotes,
        @JsonProperty("duplicate_of") PhilomenaId duplicateOf,
        @JsonProperty("duration") float duration,
        @JsonProperty("faves") int faves,
        @JsonProperty("first_seen_at") LocalDateTime firstSeenAt,
        @JsonProperty("format") String format,
        @JsonProperty("height") int height,
        @JsonProperty("hidden_from_users") boolean hiddenFromUsers,
        @JsonProperty("id") PhilomenaId id,
        // @JsonProperty("intensities") Object intensities,
        @JsonProperty("mime_type") String mimeType,
        @JsonProperty("name") String name,
        @JsonProperty("orig_sha512_hash") String origSha512Hash,
        @JsonProperty("processed") boolean processed,
        @JsonProperty("representations") Map<String, String> representations,
        @JsonProperty("score") int score,
        @JsonProperty("sha512_hash") String sha512Hash,
        @JsonProperty("size") int size,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("spoilered") boolean spoilered,
        @JsonProperty("tag_count") int tagCount,
        @JsonProperty("tag_ids") int[] tagIds,
        @JsonProperty("tags") String[] tags,
        @JsonProperty("thumbnails_generated") boolean thumbnailsGenerated,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("uploader") String uploader,
        @JsonProperty("uploader_id") int uploaderId,
        @JsonProperty("upvotes") int upvotes,
        @JsonProperty("view_url") String viewUrl,
        @JsonProperty("width") int width,
        @JsonProperty("wilson_score") float wilsonScore
) {
}
