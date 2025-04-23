package com.parkez.settlement.batch;

import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OwnerItemReader implements ItemReader<User> {

    private final UserReader userReader;

    private static final int PAGE_SIZE = 10;

    private int currentPage = 0;
    private List<User> currentChunk = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    public User read() {
        if (currentIndex >= currentChunk.size()) {
            // 다음 페이지 조회
            this.currentChunk = userReader.findAllOwnersByPage(currentPage++, PAGE_SIZE);

            // 더 이상 데이터가 없을 경우 종료
            if (currentChunk.isEmpty()) {
                return null;
            }
            currentIndex = 0;
        }
        return currentChunk.get(currentIndex++);
    }
}
