
# Created by mwang on 3/3/17.
#
#  A week before Thanksgiving, a sly turkey is hiding from a family that wants to cook it for the holiday dinner.
#  There are five boxes in a row, and the turkey hides in one of these boxes. Let's label these boxes sequentially where Box 1 is the leftmost box and Box 5 is the rightmost box. Each night,
#  the turkey moves one box to the left or right, hiding in an adjacent box the next day. Each morning, the family can look in one box to try to find the turkey.
#
#  How can the family guarantee they will find the turkey before Thanksgiving dinner?

N = 5
S = 7


def solve(actions, pos):
    if len(pos) == 0:
        return actions
    elif len(actions) >= S:
        return None
    else:
        new_pos = {x for x in ([p + 1 for p in pos] + [p - 1 for p in pos]) if 0 <= x < N}

        for i in range(0, N):
            actions.append(i)
            ret = solve(actions, {x for x in new_pos if x != i})
            if ret is not None:
                return ret
            actions.pop()

print(solve([], set(range(0, N))))
