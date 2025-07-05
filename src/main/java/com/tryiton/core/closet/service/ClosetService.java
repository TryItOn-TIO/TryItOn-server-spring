package com.tryiton.core.closet.service;

import com.tryiton.core.closet.dto.ClosetResponse;
import com.tryiton.core.member.entity.Member;
import java.util.List;

public interface ClosetService {
    void addToCloset(Member member, Long avatarId);

    void removeFromCloset(Member member, Long closetId);

    List<ClosetResponse> getClosetItems(Member member);
}
