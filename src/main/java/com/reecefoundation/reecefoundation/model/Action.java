package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="actions")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="owner_id")
    private Owner owner;

    @Column(name = "action_description", nullable = false)
    private String actionDescription;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "completed")
    private Boolean completed = false;

    // Not completed first, then by due date (soonest first, no due date last)
    public static final Comparator<Action> DISPLAY_ORDER = Comparator
            .comparing((Action a) -> Boolean.TRUE.equals(a.getCompleted()))
            .thenComparing(Action::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Action::getId, Comparator.nullsLast(Comparator.naturalOrder()));
}
