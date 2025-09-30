import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Paper
} from '@mui/material';
import { Add, Edit, Delete, Build } from '@mui/icons-material';
import client from '../../api/client';

interface Field {
  id: number;
  name: string;
}

export default function Fields() {
  const { t, i18n } = useTranslation();
  const qc = useQueryClient();
  
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingField, setEditingField] = useState<Field | null>(null);
  const [fieldName, setFieldName] = useState('');
  const [error, setError] = useState('');

  const { data: fields, isLoading } = useQuery({
    queryKey: ['fields'],
    queryFn: async () => (await client.get('/public/fields')).data
  });

  const createMutation = useMutation({
    mutationFn: async (name: string) => 
      (await client.post('/admin/fields', { name })).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['fields'] });
      handleCloseDialog();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to create field');
    }
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, name }: { id: number; name: string }) =>
      (await client.put(`/admin/fields/${id}`, { name })).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['fields'] });
      handleCloseDialog();
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to update field');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) =>
      await client.delete(`/admin/fields/${id}`),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['fields'] });
    },
    onError: (err: any) => {
      setError(err.response?.data?.message || 'Failed to delete field');
    }
  });

  const handleOpenDialog = (field?: Field) => {
    if (field) {
      setEditingField(field);
      setFieldName(field.name);
    } else {
      setEditingField(null);
      setFieldName('');
    }
    setError('');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingField(null);
    setFieldName('');
    setError('');
  };

  const handleSubmit = () => {
    if (!fieldName.trim()) {
      setError(i18n.language === 'ar' ? 'الاسم مطلوب' : 'Name is required');
      return;
    }

    if (editingField) {
      updateMutation.mutate({ id: editingField.id, name: fieldName });
    } else {
      createMutation.mutate(fieldName);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm(i18n.language === 'ar' ? 'هل أنت متأكد؟' : 'Are you sure?')) {
      deleteMutation.mutate(id);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h5" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Build />
              {i18n.language === 'ar' ? 'إدارة المجالات' : 'Manage Fields'}
            </Typography>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => handleOpenDialog()}
            >
              {i18n.language === 'ar' ? 'إضافة مجال' : 'Add Field'}
            </Button>
          </Box>

          {error && (
            <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>ID</strong></TableCell>
                  <TableCell><strong>{i18n.language === 'ar' ? 'الاسم' : 'Name'}</strong></TableCell>
                  <TableCell align="right"><strong>{i18n.language === 'ar' ? 'الإجراءات' : 'Actions'}</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(fields || []).length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} align="center">
                      <Typography color="text.secondary">
                        {i18n.language === 'ar' ? 'لا توجد مجالات' : 'No fields yet'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  (fields || []).map((field: Field) => (
                    <TableRow key={field.id}>
                      <TableCell>{field.id}</TableCell>
                      <TableCell>{field.name}</TableCell>
                      <TableCell align="right">
                        <IconButton
                          color="primary"
                          onClick={() => handleOpenDialog(field)}
                          size="small"
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          color="error"
                          onClick={() => handleDelete(field.id)}
                          size="small"
                        >
                          <Delete />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Add/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingField
            ? (i18n.language === 'ar' ? 'تعديل المجال' : 'Edit Field')
            : (i18n.language === 'ar' ? 'إضافة مجال' : 'Add Field')}
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label={i18n.language === 'ar' ? 'اسم المجال' : 'Field Name'}
            value={fieldName}
            onChange={(e) => setFieldName(e.target.value)}
            sx={{ mt: 2 }}
            autoFocus
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>
            {i18n.language === 'ar' ? 'إلغاء' : 'Cancel'}
          </Button>
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={createMutation.isPending || updateMutation.isPending}
          >
            {createMutation.isPending || updateMutation.isPending ? (
              <CircularProgress size={20} />
            ) : editingField ? (
              i18n.language === 'ar' ? 'تحديث' : 'Update'
            ) : (
              i18n.language === 'ar' ? 'إضافة' : 'Add'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}