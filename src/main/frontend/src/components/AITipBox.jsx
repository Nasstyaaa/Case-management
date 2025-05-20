import React, { useState, useEffect } from 'react';
import { Card, Typography, IconButton, Box, Tooltip, CircularProgress } from '@mui/material';
import { Lightbulb as LightbulbIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import { styled } from '@mui/material/styles';

const TipCard = styled(Card)(({ theme }) => ({
    padding: theme.spacing(2),
    marginBottom: theme.spacing(2),
    backgroundColor: theme.palette.background.default,
    border: `1px solid ${theme.palette.divider}`,
    '&:hover': {
        boxShadow: theme.shadows[4],
    },
    minHeight: '120px',
    display: 'flex',
    flexDirection: 'column'
}));

const AITipBox = ({ type, entityId }) => {
    const [tip, setTip] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchTip = async () => {
        setLoading(true);
        setError(null);
        try {
            let url = '/api/ai-tips/';
            if (type === 'task' && entityId) {
                url += `task/${entityId}`;
            } else if (type === 'board' && entityId) {
                url += `board/${entityId}`;
            } else {
                url += 'general';
            }

            const response = await fetch(url);
            if (!response.ok) {
                throw new Error('Failed to fetch tip');
            }
            const data = await response.json();
            setTip(data);
        } catch (error) {
            console.error('Error fetching AI tip:', error);
            setError('Failed to load tip. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTip();
    }, [type, entityId]);

    return (
        <TipCard>
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
                <Box display="flex" alignItems="center">
                    <LightbulbIcon color="primary" sx={{ mr: 1 }} />
                    <Typography variant="subtitle1" color="primary">
                        AI Assistant
                    </Typography>
                </Box>
                <Tooltip title="Get new tip">
                    <IconButton 
                        onClick={fetchTip} 
                        disabled={loading}
                        size="small"
                    >
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
            </Box>
            <Box flex={1} display="flex" alignItems="center" justifyContent="center">
                {loading ? (
                    <CircularProgress size={24} />
                ) : error ? (
                    <Typography color="error" variant="body2">{error}</Typography>
                ) : tip ? (
                    <Typography variant="body2" color="text.secondary">
                        {tip.content}
                    </Typography>
                ) : null}
            </Box>
        </TipCard>
    );
};

export default AITipBox; 