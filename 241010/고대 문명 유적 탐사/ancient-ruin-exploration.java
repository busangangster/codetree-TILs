import java.util.*;
import java.io.*;

public class Main {
    static int K, M;
    static int[][] grid = new int[5][5]; // 유적지 격자
    static int[] wallNumbers; // 벽면에 적힌 유물 조각 번호들
    static int wallIndex = 0; // 다음에 사용할 벽면 숫자의 인덱스
    static int[] dx = {-1, 1, 0, 0}; // 상하좌우 방향 벡터
    static int[] dy = {0, 0, -1, 1};

    static List<Integer> turnValues = new ArrayList<>(); // 각 턴마다 획득한 유물의 가치 총합

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        // 입력 받기
        st = new StringTokenizer(br.readLine());
        K = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());

        // 유적지 격자 입력
        for (int i = 0; i < 5; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < 5; j++) {
                grid[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 벽면 숫자 입력
        st = new StringTokenizer(br.readLine());
        wallNumbers = new int[M];
        for (int i = 0; i < M; i++) {
            wallNumbers[i] = Integer.parseInt(st.nextToken());
        }

        // 턴 진행
        for (int turn = 0; turn < K; turn++) {
            int maxFirstAcquisition = -1; // 유물 1차 획득 최대값
            int[] bestRotation = null; // 최적의 회전 방법 저장 [x, y, angle]
            boolean found = false; // 유물을 획득할 수 있는지 여부

            // 가능한 모든 3x3 격자 위치 탐색
            for (int x = 0; x <= 2; x++) {
                for (int y = 0; y <= 2; y++) {
                    // 가능한 모든 회전 각도 탐색
                    for (int angle : new int[]{90, 180, 270}) {
                        // 격자 복사
                        int[][] tempGrid = copyGrid(grid);

                        // 회전 적용
                        rotateSubgrid(tempGrid, x, y, angle);

                        // 유물 1차 획득 가치 계산
                        int firstAcquisition = calculateFirstAcquisition(tempGrid);

                        // 최대값 갱신 및 회전 방법 선택 기준에 따라 업데이트
                        if (firstAcquisition > maxFirstAcquisition) {
                            maxFirstAcquisition = firstAcquisition;
                            bestRotation = new int[]{x, y, angle};
                            found = true;
                        } else if (firstAcquisition == maxFirstAcquisition) {
                            // 회전 각도가 더 작은지 확인
                            if (angle < bestRotation[2]) {
                                bestRotation = new int[]{x, y, angle};
                            } else if (angle == bestRotation[2]) {
                                // 열이 더 작은지 확인
                                if (y < bestRotation[1]) {
                                    bestRotation = new int[]{x, y, angle};
                                } else if (y == bestRotation[1]) {
                                    // 행이 더 작은지 확인
                                    if (x < bestRotation[0]) {
                                        bestRotation = new int[]{x, y, angle};
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 유물을 획득할 수 없는 경우 탐사 종료
            if (!found || maxFirstAcquisition <= 0) {
                break;
            }

            // 최적의 회전 적용
            int x = bestRotation[0];
            int y = bestRotation[1];
            int angle = bestRotation[2];

            rotateSubgrid(grid, x, y, angle);

            // 유물 획득 과정 시뮬레이션
            int totalValue = 0; // 이번 턴의 유물 가치 총합

            while (true) {
                // 유물 그룹 찾기
                List<List<int[]>> groups = findGroups();

                if (groups.isEmpty()) {
                    break; // 더 이상 유물이 없으면 종료
                }

                // 유물 제거 및 가치 합산
                for (List<int[]> group : groups) {
                    totalValue += group.size();
                    // 조각 제거 (0으로 설정)
                    for (int[] pos : group) {
                        grid[pos[0]][pos[1]] = 0;
                    }
                }

                // 빈 칸 채우기
                fillEmptyCells();
            }

            // 이번 턴의 유물 가치 총합 저장
            turnValues.add(totalValue);
        }

        // 결과 출력
        for (int i = 0; i < turnValues.size(); i++) {
            System.out.print(turnValues.get(i));
            if (i < turnValues.size() - 1) {
                System.out.print(" ");
            }
        }
    }

    // 격자 복사
    static int[][] copyGrid(int[][] src) {
        int[][] dest = new int[5][5];
        for (int i = 0; i < 5; i++) {
            dest[i] = src[i].clone();
        }
        return dest;
    }

    // 3x3 부분 격자 회전
    static void rotateSubgrid(int[][] grid, int x, int y, int angle) {
        int[][] subgrid = new int[3][3];
        // 부분 격자 추출
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                subgrid[i][j] = grid[x + i][y + j];
            }
        }

        // 회전 수행
        int[][] rotated = new int[3][3];
        if (angle == 90) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rotated[j][2 - i] = subgrid[i][j];
                }
            }
        } else if (angle == 180) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rotated[2 - i][2 - j] = subgrid[i][j];
                }
            }
        } else if (angle == 270) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rotated[2 - j][i] = subgrid[i][j];
                }
            }
        }

        // 회전된 부분 격자를 원래 격자에 반영
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[x + i][y + j] = rotated[i][j];
            }
        }
    }

    // 유물 1차 획득 가치 계산
    static int calculateFirstAcquisition(int[][] grid) {
        boolean[][] visited = new boolean[5][5];
        int firstAcquisition = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!visited[i][j]) {
                    int num = grid[i][j];
                    List<int[]> group = new ArrayList<>();
                    Queue<int[]> queue = new LinkedList<>();
                    visited[i][j] = true;
                    queue.add(new int[]{i, j});
                    group.add(new int[]{i, j});

                    while (!queue.isEmpty()) {
                        int[] pos = queue.poll();
                        int ci = pos[0];
                        int cj = pos[1];

                        for (int d = 0; d < 4; d++) {
                            int ni = ci + dx[d];
                            int nj = cj + dy[d];
                            if (0 <= ni && ni < 5 && 0 <= nj && nj < 5 && !visited[ni][nj]) {
                                if (grid[ni][nj] == num) {
                                    visited[ni][nj] = true;
                                    queue.add(new int[]{ni, nj});
                                    group.add(new int[]{ni, nj});
                                }
                            }
                        }
                    }

                    if (group.size() >= 3) {
                        firstAcquisition += group.size();
                    }
                }
            }
        }

        return firstAcquisition;
    }

    // 유물 그룹 찾기
    static List<List<int[]>> findGroups() {
        boolean[][] visited = new boolean[5][5];
        List<List<int[]>> groups = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (!visited[i][j] && grid[i][j] != 0) {
                    int num = grid[i][j];
                    List<int[]> group = new ArrayList<>();
                    Queue<int[]> queue = new LinkedList<>();
                    visited[i][j] = true;
                    queue.add(new int[]{i, j});
                    group.add(new int[]{i, j});

                    while (!queue.isEmpty()) {
                        int[] pos = queue.poll();
                        int ci = pos[0];
                        int cj = pos[1];

                        for (int d = 0; d < 4; d++) {
                            int ni = ci + dx[d];
                            int nj = cj + dy[d];
                            if (0 <= ni && ni < 5 && 0 <= nj && nj < 5 && !visited[ni][nj]) {
                                if (grid[ni][nj] == num) {
                                    visited[ni][nj] = true;
                                    queue.add(new int[]{ni, nj});
                                    group.add(new int[]{ni, nj});
                                }
                            }
                        }
                    }

                    if (group.size() >= 3) {
                        groups.add(group);
                    }
                }
            }
        }

        return groups;
    }

    // 빈 칸 채우기
    static void fillEmptyCells() {
        // 빈 칸 위치 찾기
        List<int[]> emptyCells = new ArrayList<>();
        for (int j = 0; j < 5; j++) {
            for (int i = 4; i >= 0; i--) {
                if (grid[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        // 열 번호가 작은 순, 행 번호가 큰 순으로 정렬
        emptyCells.sort((a, b) -> {
            if (a[1] != b[1]) {
                return Integer.compare(a[1], b[1]); // 열 번호 오름차순
            } else {
                return Integer.compare(b[0], a[0]); // 행 번호 내림차순
            }
        });

        // 빈 칸 채우기
        for (int[] pos : emptyCells) {
            int i = pos[0];
            int j = pos[1];
            if (wallIndex < wallNumbers.length) {
                grid[i][j] = wallNumbers[wallIndex++];
            } else {
                grid[i][j] = 1; // 벽면 숫자가 부족한 경우 1로 채움 (문제에서 부족한 경우는 없다고 함)
            }
        }
    }
}