import React, { useState, useEffect } from 'react';
import { Box, Grid, Paper, Typography } from '@mui/material';
import AITipBox from './AITipBox';

const Board = ({ boardId }) => {
    const [board, setBoard] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchBoard = async () => {
            try {
                const response = await fetch(`/api/boards/${boardId}`);
                const data = await response.json();
                setBoard(data);
            } catch (error) {
                console.error('Error fetching board:', error);
            } finally {
                setLoading(false);
            }
        };

        if (boardId) {
            fetchBoard();
        }
    }, [boardId]);

    if (loading) {
        return <Box>Loading...</Box>;
    }

    if (!board) {
        return <Box>Board not found</Box>;
    }

    return (
        <Box sx={{ p: 3 }}>
            <Grid container spacing={3}>
                <Grid item xs={12}>
                    <Paper sx={{ p: 2, mb: 2 }}>
                        <Typography variant="h5" component="h1">
                            {board.name}
                        </Typography>
                        {board.description && (
                            <Typography variant="body1" color="text.secondary" mt={1}>
                                {board.description}
                            </Typography>
                        )}
                    </Paper>
                </Grid>
                <Grid item xs={12}>
                    <AITipBox type="board" entityId={boardId} />
                </Grid>
                <Grid item xs={12}>
                    {/* Board content (columns, tasks, etc.) will go here */}
                </Grid>
            </Grid>
        </Box>
    );
};

export default Board; 