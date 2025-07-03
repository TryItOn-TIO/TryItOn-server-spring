package com.tryiton.core.story.entity;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Str;

@Entity
@Getter
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member author;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;

    @Column(name = "story_image_url", nullable = false, length = 600)
    private String storyImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryLike> likes; // 이 스토리에 대한 좋아요 목록

    @Builder
    public Story(Member author, Avatar avatar, String storyImageUrl, LocalDateTime createdAt,
        String contents, int likeCount) {
        this.author = author;
        this.avatar = avatar;
        this.storyImageUrl = storyImageUrl;
        this.createdAt = createdAt;
        this.contents = contents;
        this.comments = new ArrayList<>();
        this.likeCount = likeCount;
        this.likes = new ArrayList<>();
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    // 스토리 수정
    public void update(String contents){
        this.contents = contents;
    }
}
