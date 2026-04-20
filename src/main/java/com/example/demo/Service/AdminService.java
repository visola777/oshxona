package com.example.demo.Service;

import com.example.demo.entity.Dish;
import com.example.demo.entity.TelegramUser;
import com.example.demo.entity.Vote;
import com.example.demo.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final BotUserService userService;
    private final VoteRepository voteRepository;

    public long countVotesToday() {
        return voteRepository.findAllByVoteDate(LocalDate.now()).size();
    }

    public List<TelegramUser> allUsers() {
        return userService.getAllUsers();
    }

    public byte[] exportVotesCsv() {
        List<Vote> votes = voteRepository.findAll();
        String header = "userId,username,dish,category,voteDate\n";
        String body = votes.stream()
                .map(v -> String.format("%d,%s,%s,%s,%s",
                        v.getUserId(),
                        safe(v.getDish().getName()),
                        safe(v.getDish().getCategory()),
                        safe(v.getCategory()),
                        v.getVoteDate()))
                .collect(Collectors.joining("\n"));
        return (header + body).getBytes(StandardCharsets.UTF_8);
    }

    public void resetTodayVotes() {
        List<Vote> currentVotes = voteRepository.findAllByVoteDate(LocalDate.now());
        currentVotes.forEach(vote -> {
            Dish dish = vote.getDish();
            dish.setTotalVotes(Math.max(0, dish.getTotalVotes() - 1));
        });
        voteRepository.deleteAll(currentVotes);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ").replace("\n", " ");
    }
}
