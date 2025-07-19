package com.tryiton.core.story.entity;

import com.tryiton.core.closet.entity.ClosetAvatar;
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
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member author;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closet_avatar_id", nullable = false)
    private ClosetAvatar closetAvatar;

    @Column(name = "story_image_url", nullable = false, length = 600)
    private String storyImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 10)
    private List<Comment> comments;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 10)
    private List<StoryLike> likes; // 이 스토리에 대한 좋아요 목록

    @Builder
    public Story(Member author, ClosetAvatar closetAvatar, String storyImageUrl, LocalDateTime createdAt, LocalDateTime updatedAt,
        String contents, int likeCount) {
        this.author = author;
        this.closetAvatar = closetAvatar;
        this.storyImageUrl = storyImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    public void update(String contents, LocalDateTime updatedAt){
        this.contents = contents;
        this.updatedAt = updatedAt;
    }
}
